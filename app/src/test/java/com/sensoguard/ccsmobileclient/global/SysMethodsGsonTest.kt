package com.sensoguard.ccsmobileclient.global

import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.classes.SystemSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SysMethodsGsonTest {

    @Test
    fun alarmsJson_roundTrip_keepsTheFields() {
        val alarm = Alarm("gw-1", "gw-1", ALARM_CAR_STR, "10:00:00 01/01/26", true, 1000L, "1", true)
        alarm.latitude = 32.1
        alarm.longitude = 34.8
        alarm.isLocallyDefined = true

        val alarms = convertJsonToAlarmList(convertToAlarmsGson(arrayListOf(alarm)))

        assertNotNull(alarms)
        assertEquals(1, alarms!!.size)
        val result = alarms[0]
        assertEquals("gw-1", result.id)
        assertEquals(ALARM_CAR_STR, result.type)
        assertEquals("1", result.zone)
        assertEquals(1000L, result.timeInMillis)
        assertEquals(32.1, result.latitude!!, 0.0)
        assertEquals(34.8, result.longitude!!, 0.0)
        assertTrue(result.isNewSystem)
        assertTrue(result.isLocallyDefined)
    }

    @Test
    fun alarmsJson_ofOlderVersion_isStillRead() {
        //alarms that have been stored by older versions contain fields that have been removed
        val json = """[{"id":"gw-2","name":"gw-2","type":"Digging","currentDate":"d",
            |"isArmed":true,"timeInMillis":5,"zone":"2","isNewSystem":false,
            |"isActive":false,"isReadyToDelete":false}]""".trimMargin()

        val alarms = convertJsonToAlarmList(json)

        assertEquals(1, alarms!!.size)
        assertEquals("gw-2", alarms[0].id)
        assertEquals("2", alarms[0].zone)
    }

    @Test
    fun systemSortJson_roundTrip() {
        val json = convertSystemSortToGson(arrayListOf(SystemSort("1", true), SystemSort("2", false)))

        val systems = convertJsonToSystemSortList(json)

        assertEquals(2, systems.size)
        assertEquals("1", systems[0].unit)
        assertEquals(true, systems[0].isSorted)
        assertEquals(false, systems[1].isSorted)
    }

    @Test
    fun systemSortJson_empty_returnsEmptyList() {
        assertTrue(convertJsonToSystemSortList("").isEmpty())
    }
}
