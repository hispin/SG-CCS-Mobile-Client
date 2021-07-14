package com.sensoguard.ccsmobileclient.interfaces

import com.sensoguard.ccsmobileclient.classes.Sensor

interface OnAdapterListener {
    fun saveNameSensor(detector: Sensor)
    fun saveSensors(detector: Sensor)
}