package com.sensoguard.ccsmobileclient.global

import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import java.util.concurrent.CopyOnWriteArrayList

class UserSession private constructor() {

    //list of sensors alarm (thread safe: it is changed by the push messages, the timer and the UI)
    val alarmSensors: MutableList<AlarmSensor> = CopyOnWriteArrayList()

    private object Holder {
        val INSTANCE = UserSession()
    }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }
}
