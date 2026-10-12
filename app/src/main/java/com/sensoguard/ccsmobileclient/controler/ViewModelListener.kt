package com.sensoguard.ccsmobileclient.controler

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class ViewModelListener(application: Application) : AndroidViewModel(application) {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null


    //listener to changes when the timer interval is time out
    private val currentCalendar = MutableLiveData<Calendar>()

    fun startCurrentCalendarListener(): LiveData<Calendar> {
        return currentCalendar
    }


    fun startTimer() {
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if (scheduleTaskExecutor?.isShutdown != false) {
            scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor()
            executeTimer()
        }
    }

    // execute the timer every second
    private fun executeTimer() {
        scheduleTaskExecutor?.scheduleWithFixedDelay({
            try {
                //check if there is alarm sensor that already timeout
                setSoundOffForTimeoutAlarms()

                //if there is no alarm sensor the stop timer and sound of the alarm
                val alarmSensors = UserSession.instance.alarmSensors
                if (alarmSensors.isEmpty() || alarmSensors.none { it.isSound }) {
                    //stop the timer
                    shutDownTimer()
                    //stop the sound alarm
                    val context = getApplication<Application>().applicationContext
                    context.sendBroadcast(
                        Intent(STOP_ALARM_SOUND).setPackage(context.packageName)
                    )
                }
                // update screen if showed
                currentCalendar.postValue(Calendar.getInstance())

            } catch (ex: RuntimeException) {
                Log.e(TAG, "timer exception", ex)
            }

        }, 0, 1, TimeUnit.SECONDS)
    }

    //check if there is alarm sensor that already timeout then set sound as off
    private fun setSoundOffForTimeoutAlarms() {
        UserSession.instance.alarmSensors
            .filter { isSensorAlarmTimeout(it) }
            .forEach { it.isSound = false }
    }

    //shut down the timer
    fun shutDownTimer() {
        Log.d(TAG, "shutDownTimer")
        scheduleTaskExecutor?.shutdownNow()
    }

    override fun onCleared() {
        shutDownTimer()
    }

    //check if the alarm sensor is in duration
    private fun isSensorAlarmTimeout(alarmProcess: AlarmSensor): Boolean {
        val timeout = getLongInPreference(
            getApplication<Application>().applicationContext,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        ) ?: return true
        val futureTimeout = alarmProcess.alarmTime.timeInMillis + timeout * 1000
        return futureTimeout < Calendar.getInstance().timeInMillis
    }

    companion object {
        private const val TAG = "ViewModelListener"
    }
}
