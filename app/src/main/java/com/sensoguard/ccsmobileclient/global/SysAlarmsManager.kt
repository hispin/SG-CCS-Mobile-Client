package com.sensoguard.ccsmobileclient.global

import android.content.Context
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import java.util.*

//get the alarms from locally
fun populateAlarmsFromLocally(context: Context): ArrayList<Alarm>? {
    val alarms: ArrayList<Alarm>?
    val alarmListStr = getStringInPreference(context, ALARM_LIST_KEY_PREF, ERROR_RESP)

    alarms = if (alarmListStr.equals(ERROR_RESP)) {
        ArrayList()
    } else {
        alarmListStr?.let { convertJsonToAlarmList(it) }
    }
    return alarms
}

/**
 * store the detectors to locally
 */
fun storeAlarmsToLocally(alarms: ArrayList<Alarm>, context: Context) {
    // sort the list of events by date in descending
    val alarms = ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
    if (alarms != null && alarms.size > 0) {
        val alarmsJsonStr = convertToAlarmsGson(alarms)
        setStringInPreference(context, ALARM_LIST_KEY_PREF, alarmsJsonStr)
    }
}

/**
 * add alarm
 */
fun addAlarmToQueue(
    alarmId: String,
    lat: Double,
    lon: Double,
    type: String,
    isArmed: Boolean,
    typeIndex: Int,
    zone: String

) {
    //////////////add alarm to queue
    //prevent duplicate alarm at the same sensor at the same time
    removeSensorAlarmById(alarmId)
    if (type != null) {
        val sensorAlarm = AlarmSensor(
            alarmId,
            Calendar.getInstance(),
            type,
            isArmed,
            zone
        )
        sensorAlarm.typeIdx = typeIndex
        sensorAlarm.latitude = lat
        sensorAlarm.longitude = lon
        UserSession.instance.alarmSensors?.add(sensorAlarm)
    }
    /// end add to queue
}

//remove alarm sensor if exist
fun removeSensorAlarmById(alarmId: String) {

    val iteratorList = UserSession.instance.alarmSensors?.listIterator()
    while (iteratorList != null && iteratorList.hasNext()) {
        val sensorItem = iteratorList.next()
        if (sensorItem.alarmSensorId == alarmId) {
            iteratorList.remove()
        }
    }
}