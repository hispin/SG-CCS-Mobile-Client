package com.sensoguard.ccsmobileclient.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.EmailService
import com.sensoguard.ccsmobileclient.global.ALARM_TYPE_INDEX_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_ID_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_NOT_DEFINED_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.IS_FORWARD_ALARM_EMAIL
import com.sensoguard.ccsmobileclient.global.IS_SSL_MAIL
import com.sensoguard.ccsmobileclient.global.PASSWORD_MAIL
import com.sensoguard.ccsmobileclient.global.PORT_MAIL
import com.sensoguard.ccsmobileclient.global.READ_DATA_KEY
import com.sensoguard.ccsmobileclient.global.READ_DATA_KEY_TEST
import com.sensoguard.ccsmobileclient.global.RECIPIENT_MAIL
import com.sensoguard.ccsmobileclient.global.SEISMIC_TYPE
import com.sensoguard.ccsmobileclient.global.SERVER_MAIL
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
import com.sensoguard.ccsmobileclient.global.USER_NAME_MAIL
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getStrDateTimeByMilliSeconds
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.mail.internet.InternetAddress


class ServiceHandleAlarms : ParentService() {
    private val TAG = "ServiceHandleAlarms"


    override fun onCreate() {
        super.onCreate()
        startSysForeGround()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopPlayingAlarm()
        this@ServiceHandleAlarms.stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //FusedLocationProviderClient is for interacting with the location using fused location
        setFilter()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun setFilter() {
        val filter = IntentFilter(READ_DATA_KEY)
        filter.addAction(READ_DATA_KEY_TEST)
        filter.addAction(STOP_ALARM_SOUND)
        filter.addAction(CREATE_ALARM_KEY)
        filter.addAction(CREATE_ALARM_NOT_DEFINED_KEY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("accept alarm")
            when (intent.action) {
                CREATE_ALARM_KEY -> {
                    val alarmSensorId = intent.getStringExtra(CREATE_ALARM_ID_KEY)
                    val type = intent.getStringExtra(CREATE_ALARM_TYPE_KEY)
                    val alarmTypeId = intent.getLongExtra(ALARM_TYPE_INDEX_KEY, -1)
                    val date = getStrDateTimeByMilliSeconds(
                        Calendar.getInstance().timeInMillis,
                        "dd/MM/yy kk:mm:ss",
                        context
                    )

                    val msg = resources.getString(
                        R.string.email_content,
                        type,
                        alarmSensorId,
                        date
                    )

                    Toast.makeText(
                        context,
                        "$type alarm from unit $alarmSensorId ",
                        Toast.LENGTH_LONG
                    )
                        .show()

                    sendEmailBakground(msg)
                    //play sound and vibrate
                    //playAlarmSound()
                    //playVibrate()

                }
                CREATE_ALARM_NOT_DEFINED_KEY -> {
                    val alarmSensorId = intent.getStringExtra(CREATE_ALARM_ID_KEY)
                    val type = intent.getStringExtra(CREATE_ALARM_TYPE_KEY)
                    val sensorTypeId = intent.getLongExtra(ALARM_TYPE_INDEX_KEY, -1)
                    if (sensorTypeId == SEISMIC_TYPE) {
                        Toast.makeText(
                            context,
                            "$type alarm from unit $alarmSensorId ",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        //accept test alarm (for testing)
                    } else {
                        val values = resources.getStringArray(R.array.sensor_type)

                        val idx = sensorTypeId.toInt()
                        var sensorType = ""
                        if (idx >= 0 && idx < values.size) {
                            sensorType = values[idx]
                        }

                        Toast.makeText(
                            context,
                            "$sensorType alarm from unit $alarmSensorId ",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                STOP_ALARM_SOUND -> {
                    stopPlayingAlarm()
                }
            }
        }
    }


    //send alarm email
    private fun sendEmailBakground(msg: String) {

        //check if forward alarm email is active
        val isForwardAlarmEmail = getBooleanInPreference(this, IS_FORWARD_ALARM_EMAIL, false)
        if (!isForwardAlarmEmail) {
            return
        }

        val userName = getStringInPreference(this, USER_NAME_MAIL, "-1")
        val password = getStringInPreference(this, PASSWORD_MAIL, "-1")
        val recipient = getStringInPreference(this, RECIPIENT_MAIL, "-1")
        val server = getStringInPreference(this, SERVER_MAIL, "-1")
        val port = getIntInPreference(this, PORT_MAIL, -1)
        val isSSL = getBooleanInPreference(this, IS_SSL_MAIL, false)

        //check if the account mail has been filled
        if (userName.equals("-1") || password.equals("-1")
            || recipient.equals("-1") || server.equals("-1")
            || port == -1
        ) {
            //Bugs fixed : cancel this message
            //showToast(this, resources.getString(R.string.no_fill_account))
            return
        }

        val auth = EmailService.UserPassAuthenticator(userName!!, password!!)//sg-patrol@sgsmtp.com
        val to = listOf(InternetAddress(recipient))
        val from = InternetAddress(userName)
        val email = EmailService.Email(
            auth,
            to,
            from,
            resources.getString(R.string.an_alert_was_received),
            msg
        )
        val emailService = EmailService(server!!, port!!)//("mail.sgsmtp.com", port!!)


        //TODO ssl=0
        //use CoroutineScope to prevent blocking main thread
        GlobalScope.launch { // or however you do background threads
            emailService.send(email, isSSL)
        }
    }


    private var rington: Ringtone? = null

    private fun stopPlayingAlarm() {
        if (rington != null && rington?.isPlaying!!) {
            rington?.stop()
        }
    }


    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        fun getNotificationIcon(): Int {
            val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.drawable.ic_app_notification else R.mipmap.ic_launcher
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val `object` = getSystemService(Context.NOTIFICATION_SERVICE)
            if (`object` != null && `object` is NotificationManager) {
                `object`.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("SG-CCS Mobile Client is running")
                    .setSmallIcon(getNotificationIcon())
                    .build()

                startForeground(1, notification)
            }
        }


}