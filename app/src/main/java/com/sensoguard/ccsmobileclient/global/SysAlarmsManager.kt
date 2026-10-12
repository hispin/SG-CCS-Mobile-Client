package com.sensoguard.ccsmobileclient.global

import android.content.Context
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import java.util.Calendar

//get the alarms from locally
fun populateAlarmsFromLocally(context: Context?): ArrayList<Alarm>? {
    val alarmListStr = getStringInPreference(context, ALARM_LIST_KEY_PREF, ERROR_RESP)

    return if (alarmListStr == null || alarmListStr == ERROR_RESP) {
        ArrayList()
    } else {
        convertJsonToAlarmList(alarmListStr)
    }
}

/**
 * store the alarms to locally (also an empty list, when all the alarms have been deleted)
 */
fun storeAlarmsToLocally(alarms: ArrayList<Alarm>, context: Context?) {
    // sort the list of events by date in descending
    val sortedAlarms = ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
    val alarmsJsonStr = convertToAlarmsGson(sortedAlarms)
    setStringInPreference(context, ALARM_LIST_KEY_PREF, alarmsJsonStr)
}

/**
 * add alarm
 */
fun addAlarmToQueue(
    alarmId: String,
    lat: Double,
    lon: Double,
    type: String,
    typeIndex: Int,
    zone: String
) {
    //prevent duplicate alarm at the same sensor at the same time
    removeSensorAlarmById(alarmId)
    val sensorAlarm = AlarmSensor(
        alarmId,
        Calendar.getInstance(),
        type,
        zone
    )
    sensorAlarm.typeIdx = typeIndex
    sensorAlarm.latitude = lat
    sensorAlarm.longitude = lon
    UserSession.instance.alarmSensors.add(sensorAlarm)
}

//remove alarm sensor if exist
fun removeSensorAlarmById(alarmId: String) {
    UserSession.instance.alarmSensors.removeIf { it.alarmSensorId == alarmId }
}
