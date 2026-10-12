package com.sensoguard.ccsmobileclient.global

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//the locale of the app (the language that has been selected)
private fun getAppLocale(context: Context): Locale {
    val resources = context.resources
    return resources.configuration.locales.getFirstMatch(resources.assets.locales)
        ?: Locale.getDefault()
}

fun getStringFromCalendar(calendar: Calendar, format: String, context: Context): String {
    val dateFormat = SimpleDateFormat(format, getAppLocale(context))//"kk:mm dd/MM/yy"
    return dateFormat.format(calendar.time)
}

//get string format by current date time in milliseconds format
fun getStrDateTimeByMilliSeconds(milliSeconds: Long, format: String, context: Context): String {
    val dateFormat = SimpleDateFormat(format, getAppLocale(context))
    return dateFormat.format(Date(milliSeconds))
}
