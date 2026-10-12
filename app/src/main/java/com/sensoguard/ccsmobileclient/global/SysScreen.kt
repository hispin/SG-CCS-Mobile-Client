package com.sensoguard.ccsmobileclient.global

import android.content.Context


//Get the width of current screen
fun getScreenWidth(context: Context?): Int {
    if (context == null) {
        return -1
    }
    return context.resources.displayMetrics.widthPixels
}
