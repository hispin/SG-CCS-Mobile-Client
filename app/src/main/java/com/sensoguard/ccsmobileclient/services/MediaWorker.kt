package com.sensoguard.ccsmobileclient.services

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sensoguard.ccsmobileclient.global.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private var rington: Ringtone? = null
    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    override fun doWork(): Result {
        playVibrate()
        if (playAlarmSound()) {
            shutDownTimer()
            startTimer()
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
            // Get instance of Vibrator from current Context
            //val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }


            // Vibrate for 200 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(1000)

            }
            Thread.sleep(1000)
        }

    }

    /**
     * stop play the sound of alarm
     */
    private fun stopPlayingAlarm() {
        if (rington != null && rington?.isPlaying!!) {
            rington?.stop()
        }
    }

    /**
     * execute play sound
     */
    private fun playAlarmSound(): Boolean {

        val isNotificationSound =
            getBooleanInPreference(context, IS_NOTIFICATION_SOUND_KEY, true)
        if (!isNotificationSound) {
            return false
        }

        val selectedSound =
            getStringInPreference(context, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (!selectedSound.equals("-1")) {

            try {
                val uri = Uri.parse(selectedSound)
                if (rington != null && rington!!.isPlaying) {
                    //if the sound it is already played,
                    rington?.stop()
                    Handler().postDelayed({
                        rington = RingtoneManager.getRingtone(context, uri)
                        rington?.play()
                    }, 1000)
                } else {
                    rington = RingtoneManager.getRingtone(context, uri)
                    rington?.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    /**
     * start timer
     */
    private fun startTimer() {
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null
            || (scheduleTaskExecutor?.isShutdown != null && scheduleTaskExecutor?.isShutdown!!)
        ) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    /**
     * execute the time
     */
    private fun executeTimer() {

        val timeout = getLongInPreference(
            context,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        // This schedule a task to run every 1 second:
        if (timeout != null) {
            scheduleTaskExecutor?.scheduleAtFixedRate({
                stopPlayingAlarm()
                shutDownTimer()
                //stopSelf()

            }, timeout, timeout, TimeUnit.SECONDS)
        }

    }


    /**
     * shut down the timer
     */
    private fun shutDownTimer() {
        try {
            scheduleTaskExecutor?.shutdownNow()
        } catch (ex: Exception) {
        } finally {

        }
    }


    override fun onStopped() {
        super.onStopped()
        stopPlayingAlarm()
        shutDownTimer()
    }
}