package com.sensoguard.ccsmobileclient.services

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import com.sensoguard.ccsmobileclient.global.getStringInPreference

/**
 * vibrate and play the sound of the alarm for the duration of the alarm
 */
class MediaWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @Volatile
    private var ringtone: Ringtone? = null

    override fun doWork(): Result {
        playVibrate()
        if (playAlarmSound()) {
            waitForAlarmDuration()
            stopPlayingAlarm()
        }
        return Result.success()
    }

    /**
     * execute vibrate
     */
    private fun playVibrate() {
        val isVibrateWhenAlarm =
            getBooleanInPreference(context, IS_VIBRATE_WHEN_ALARM_KEY, true)
        if (isVibrateWhenAlarm) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Vibrate for 1 second
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATE_DURATION_MILLIS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
            Thread.sleep(VIBRATE_DURATION_MILLIS)
        }
    }

    /**
     * wait until the duration of the alarm is over or the work is stopped
     */
    private fun waitForAlarmDuration() {
        val timeoutSeconds = getLongInPreference(
            context,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        ) ?: ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        val endTime = System.currentTimeMillis() + timeoutSeconds * 1000
        try {
            while (!isStopped && System.currentTimeMillis() < endTime) {
                Thread.sleep(CHECK_INTERVAL_MILLIS)
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * stop play the sound of alarm
     */
    private fun stopPlayingAlarm() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        ringtone = null
    }

    /**
     * execute play sound
     * @return true if the sound is enabled
     */
    private fun playAlarmSound(): Boolean {

        val isNotificationSound =
            getBooleanInPreference(context, IS_NOTIFICATION_SOUND_KEY, true)
        if (!isNotificationSound) {
            return false
        }

        val selectedSound =
            getStringInPreference(context, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (selectedSound != null && selectedSound != "-1") {
            ringtone = RingtoneManager.getRingtone(context, selectedSound.toUri())
            if (ringtone == null) {
                Log.w(TAG, "the selected sound is not available")
            }
            //play as alarm (the alarm volume, also in silent mode). since android 17 a sound
            //of an app in background is allowed only for alarms or with foreground service
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.play()
        }
        return true
    }

    override fun onStopped() {
        super.onStopped()
        stopPlayingAlarm()
    }

    companion object {
        private const val TAG = "MediaWorker"
        private const val VIBRATE_DURATION_MILLIS = 1000L
        private const val CHECK_INTERVAL_MILLIS = 200L

        //the unique name of the work, a new alarm replaces the sound of the previous alarm
        const val UNIQUE_WORK_NAME = "alarmSound"
    }
}
