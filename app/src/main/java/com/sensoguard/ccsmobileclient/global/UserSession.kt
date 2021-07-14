package com.sensoguard.ccsmobileclient.global

import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.classes.Command

class UserSession private constructor() {

    //list of sensors alarm
    var alarmSensors: ArrayList<AlarmSensor>? = ArrayList()
    var commandContent: IntArray? = null
    var myCommand: Command? = null

    private object Holder {
        val INSTANCE = UserSession()
    }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }
}