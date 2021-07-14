package com.sensoguard.ccsmobileclient.classes

class Alarm(
    var id: String?,
    var name: String?,
    var type: String?,
    var currentDate: String,
    var isArmed: Boolean?,
    var timeInMillis: Long?
){
    var longitude: Double?=null
    var latitude: Double?=null
    var isLocallyDefined:Boolean=false
    var isActive:Boolean?=false
}