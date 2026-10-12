package com.sensoguard.ccsmobileclient.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.EmailService
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_ID_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.IS_FORWARD_ALARM_EMAIL
import com.sensoguard.ccsmobileclient.global.IS_SSL_MAIL
import com.sensoguard.ccsmobileclient.global.PASSWORD_MAIL
import com.sensoguard.ccsmobileclient.global.PORT_MAIL
import com.sensoguard.ccsmobileclient.global.RECIPIENT_MAIL
import com.sensoguard.ccsmobileclient.global.SERVER_MAIL
import com.sensoguard.ccsmobileclient.global.USER_NAME_MAIL
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getStrDateTimeByMilliSeconds
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

/**
 * keep the app running for listening to the alarms (show toast and forward the alarm by email)
 */
class ServiceHandleAlarms : ParentService() {

    //the scope of sending the emails in background
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        setFilter()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startSysForeGround()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmReceiver)
        serviceScope.cancel()
    }

    private fun setFilter() {
        ContextCompat.registerReceiver(
            this,
            alarmReceiver,
            IntentFilter(CREATE_ALARM_KEY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == CREATE_ALARM_KEY) {
                val alarmSensorId = intent.getStringExtra(CREATE_ALARM_ID_KEY)
                val type = intent.getStringExtra(CREATE_ALARM_TYPE_KEY)
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
                    resources.getString(R.string.alarm_from_unit, type, alarmSensorId),
                    Toast.LENGTH_LONG
                ).show()

                sendEmailBackground(msg)
            }
        }
    }


    //send alarm email
    private fun sendEmailBackground(msg: String) {

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
        if (userName == null || userName == "-1" || password == null || password == "-1"
            || recipient == null || recipient == "-1" || server == null || server == "-1"
            || port == null || port == -1
        ) {
            return
        }

        val email = try {
            EmailService.Email(
                EmailService.UserPassAuthenticator(userName, password),
                listOf(InternetAddress(recipient)),
                InternetAddress(userName),
                resources.getString(R.string.an_alert_was_received),
                msg
            )
        } catch (ex: AddressException) {
            Log.e(TAG, "invalid email address", ex)
            return
        }
        val emailService = EmailService(server, port)

        //send in background to prevent blocking main thread
        serviceScope.launch {
            emailService.send(email, isSSL)
        }
    }


    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("SG-CCS Mobile Client is running")
            .setSmallIcon(R.drawable.ic_app_notification)
            .build()

        startForeground(1, notification)
    }

    companion object {
        private const val TAG = "ServiceHandleAlarms"

        //the channel of the notification of the running service
        const val CHANNEL_ID = "my_channel_01"
    }
}
