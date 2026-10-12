package com.sensoguard.ccsmobileclient

import android.content.Context
import android.app.NotificationManager
import android.content.ContextWrapper
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import com.sensoguard.ccsmobileclient.global.ALARM_CAR
import com.sensoguard.ccsmobileclient.global.ALARM_CAR_STR
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.populateAlarmsFromLocally
import com.sensoguard.ccsmobileclient.services.MediaWorker
import com.sensoguard.ccsmobileclient.services.MyFirebaseMessagingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * deliver a push message of alarm (as sent by the server) to the messaging service
 */
@RunWith(AndroidJUnit4::class)
class AlarmPushTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun createService(): MyFirebaseMessagingService {
        val service = MyFirebaseMessagingService()
        //attach the context of the app (as done by the system when the service is created)
        val attach = ContextWrapper::class.java.getDeclaredMethod("attachBaseContext", Context::class.java)
        attach.isAccessible = true
        attach.invoke(service, context)
        return service
    }

    @Test
    fun alarmOfNewSystem_isAddedToTheMapAndToTheLog() {
        val historySize = populateAlarmsFromLocally(context)?.size ?: 0
        val message = RemoteMessage.Builder("test@fcm.googleapis.com")
            .addData("title", "alarm")
            .addData("gateway", "TestGateway")
            .addData("alarmType", ALARM_CAR_STR)
            .addData("unit", "7")
            .addData("latitude", "32.9387")
            .addData("longitude", "35.7964")
            .build()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            createService().onMessageReceived(message)
        }

        val alarm = UserSession.instance.alarmSensors.firstOrNull { it.alarmSensorId == "TestGateway" }
        assertNotNull(alarm)
        assertEquals(ALARM_CAR, alarm!!.typeIdx)
        assertEquals("7", alarm.zone)
        val history = populateAlarmsFromLocally(context)
        assertEquals(historySize + 1, history!!.size)
        assertTrue(history.any { it.id == "TestGateway" && it.isNewSystem })

        //the notification of the alarm is shown
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        assertTrue(notificationManager.activeNotifications.any {
            it.notification.extras.getCharSequence("android.text")?.contains("TestGateway") == true
        })

        //the sound of the alarm is played until the end of the alarm duration
        assertEquals(WorkInfo.State.SUCCEEDED, waitForAlarmSound())
    }

    //wait until the work of the alarm sound is finished
    private fun waitForAlarmSound(): WorkInfo.State? {
        val workManager = WorkManager.getInstance(context)
        val endTime = System.currentTimeMillis() + 20_000
        var state: WorkInfo.State? = null
        while (System.currentTimeMillis() < endTime) {
            state = workManager.getWorkInfosForUniqueWork(MediaWorker.UNIQUE_WORK_NAME).get()
                .firstOrNull()?.state
            if (state?.isFinished == true) {
                return state
            }
            Thread.sleep(200)
        }
        return state
    }

    @Test
    fun messageWithoutLocation_isIgnoredWithoutCrash() {
        val message = RemoteMessage.Builder("test@fcm.googleapis.com")
            .addData("gateway", "NoLocationGateway")
            .addData("alarmType", ALARM_CAR_STR)
            .build()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            createService().onMessageReceived(message)
        }

        assertTrue(UserSession.instance.alarmSensors.none { it.alarmSensorId == "NoLocationGateway" })
    }
}
