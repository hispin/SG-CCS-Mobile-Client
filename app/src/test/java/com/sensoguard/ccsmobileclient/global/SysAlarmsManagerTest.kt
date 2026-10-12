package com.sensoguard.ccsmobileclient.global

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class SysAlarmsManagerTest {

    @After
    fun clearAlarms() {
        UserSession.instance.alarmSensors.clear()
    }

    @Test
    fun addAlarmToQueue_addsTheAlarm() {
        addAlarmToQueue("gw-1", 32.0, 34.0, ALARM_CAR_STR, ALARM_CAR, "1")

        val alarmSensors = UserSession.instance.alarmSensors
        assertEquals(1, alarmSensors.size)
        assertEquals("gw-1", alarmSensors[0].alarmSensorId)
        assertEquals(ALARM_CAR, alarmSensors[0].typeIdx)
        assertEquals(32.0, alarmSensors[0].latitude!!, 0.0)
    }

    @Test
    fun addAlarmToQueue_sameSensor_replacesThePreviousAlarm() {
        addAlarmToQueue("gw-1", 32.0, 34.0, ALARM_CAR_STR, ALARM_CAR, "1")
        addAlarmToQueue("gw-2", 31.0, 35.0, ALARM_DIGGING_STR, ALARM_MOTION, "2")
        addAlarmToQueue("gw-1", 33.0, 34.0, ALARM_FOOTSTEPS_STR, ALARM_INTRUDER, "1")

        val alarmSensors = UserSession.instance.alarmSensors
        assertEquals(2, alarmSensors.size)
        val alarm = alarmSensors.first { it.alarmSensorId == "gw-1" }
        assertEquals(ALARM_INTRUDER, alarm.typeIdx)
        assertEquals(33.0, alarm.latitude!!, 0.0)
    }
}
