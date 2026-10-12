package com.sensoguard.ccsmobileclient.global

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.classes.SystemSort

private const val TAG = "SysMethodsGson"

//convert json to list of SystemSort
fun convertJsonToSystemSortList(inputJsonArrayString: String): ArrayList<SystemSort> {
    //if the json string is empty, then return empty array list
    if (inputJsonArrayString.isEmpty()) {
        return ArrayList()
    }

    return try {
        val listType = object : TypeToken<ArrayList<SystemSort>>() {}.type
        Gson().fromJson<ArrayList<SystemSort>>(inputJsonArrayString, listType) ?: ArrayList()
    } catch (e: JsonParseException) {
        Log.e(TAG, "convertJsonToSystemSortList", e)
        ArrayList()
    }
}


fun convertSystemSortToGson(systemSortArr: ArrayList<SystemSort>): String {
    return Gson().toJson(systemSortArr)
}


fun convertToAlarmsGson(alarmsArr: ArrayList<Alarm>): String {
    return Gson().toJson(alarmsArr)
}

//convert json to list of Alarms
fun convertJsonToAlarmList(inputJsonArrayString: String): ArrayList<Alarm>? {
    return try {
        val listType = object : TypeToken<ArrayList<Alarm>>() {}.type
        Gson().fromJson(inputJsonArrayString, listType)
    } catch (e: JsonParseException) {
        Log.e(TAG, "convertJsonToAlarmList", e)
        null
    }
}
