package com.sensoguard.ccsmobileclient.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.global.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaService : Service() {

    override fun onCreate() {
        super.onCreate()
        startSysForeGround()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playVibrate()
        if (playAlarmSound()) {
            shutDownTimer()
            startTimer()
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    //execute vibrate
    private fun playVibrate() {

        val isVibrateWhenAlarm =
            getBooleanInPreference(this@MediaService, IS_VIBRATE_WHEN_ALARM_KEY, true)
        if (isVibrateWhenAlarm) {
            // Get instance of Vibrator from current Context
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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

    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        fun getNotificationIcon(): Int {
            val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.drawable.ic_app_notification else R.mipmap.ic_launcher
        }

        val CHANNEL_ID = "my_channel_01"
        if (Build.VERSION.SDK_INT >= 26) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val `object` = getSystemService(Context.NOTIFICATION_SERVICE)
            if (`object` != null && `object` is NotificationManager) {
                `object`.createNotificationChannel(channel)
            }
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("CCS Client is running")
            .setSmallIcon(getNotificationIcon())
            .build()

        startForeground(1, notification)

    }

    private var rington: Ringtone? = null

    //stop play the sound of alarm
    private fun stopPlayingAlarm() {
        if (rington != null && rington?.isPlaying!!) {
            rington?.stop()
        }
    }

    //execute vibrate
    private fun playAlarmSound(): Boolean {

        val isNotificationSound =
            getBooleanInPreference(this@MediaService, IS_NOTIFICATION_SOUND_KEY, true)
        if (!isNotificationSound) {
            return false
        }

        val selectedSound =
            getStringInPreference(this@MediaService, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (!selectedSound.equals("-1")) {

            try {
                val uri = Uri.parse(selectedSound)
                if (rington != null && rington!!.isPlaying) {
                    //if the sound it is already played,
                    rington?.stop()
                    Handler().postDelayed({
                        rington = RingtoneManager.getRingtone(this@MediaService, uri)
                        rington?.play()
                    }, 1000)
                } else {
                    rington = RingtoneManager.getRingtone(this@MediaService, uri)
                    rington?.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun startTimer() {
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null
            || (scheduleTaskExecutor?.isShutdown != null && scheduleTaskExecutor?.isShutdown!!)
        ) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    // execute the time
    private fun executeTimer() {

        val timeout = getLongInPreference(
            this,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        // This schedule a task to run every 1 second:
        if (timeout != null) {
            scheduleTaskExecutor?.scheduleAtFixedRate({
                stopPlayingAlarm()
                shutDownTimer()
                stopSelf()

            }, timeout, timeout, TimeUnit.SECONDS)
        }

    }

    //shut down the timer
    private fun shutDownTimer() {
        try {
            scheduleTaskExecutor?.shutdownNow()
        } catch (ex: Exception) {
        } finally {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayingAlarm()
        shutDownTimer()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}