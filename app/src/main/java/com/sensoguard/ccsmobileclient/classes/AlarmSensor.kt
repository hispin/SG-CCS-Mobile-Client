package com.sensoguard.ccsmobileclient.classes

import java.util.Calendar

class AlarmSensor(
    var alarmSensorId: String,
    var alarmTime: Calendar,
    var type: String,
    var zone: String
) {
    var typeIdx: Int? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var isSound = true
}
