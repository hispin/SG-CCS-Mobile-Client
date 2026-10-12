package com.sensoguard.ccsmobileclient.global

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
}

//read Long share preference
fun getLongInPreference(context: Context?, key: String, default: Long): Long? {
    if (context == null) {
        return null
    }
    return getPreferences(context).getLong(key, default)
}


//write Long to share preference
fun setLongInPreference(context: Context?, key: String, value: Long) {
    if (context == null) {
        return
    }
    getPreferences(context).edit { putLong(key, value) }
}


//write String to share preference
fun setStringInPreference(context: Context?, key: String, value: String?) {
    if (context == null) {
        return
    }
    getPreferences(context).edit { putString(key, value) }
}


//read String share preference
fun getStringInPreference(context: Context?, key: String, default: String?): String? {
    if (context == null) {
        return null
    }
    return getPreferences(context).getString(key, default)
}

//read Boolean share preference
fun getBooleanInPreference(context: Context?, key: String, default: Boolean): Boolean {
    if (context == null) {
        return false
    }
    return getPreferences(context).getBoolean(key, default)
}


//write Boolean to share preference
fun setBooleanInPreference(context: Context?, key: String, value: Boolean) {
    if (context == null) {
        return
    }
    getPreferences(context).edit { putBoolean(key, value) }
}

//read Int share preference
fun getIntInPreference(context: Context?, key: String, default: Int): Int? {
    if (context == null) {
        return null
    }
    return getPreferences(context).getInt(key, default)
}


//write Int to share preference
fun setIntInPreference(context: Context?, key: String, value: Int) {
    if (context == null) {
        return
    }
    getPreferences(context).edit { putInt(key, value) }
}
