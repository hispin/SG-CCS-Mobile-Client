package com.sensoguard.ccsmobileclient.global

import android.content.Context
import android.widget.Toast


fun toastNotify(notificationMessage: String?, context: Context) {
    Toast.makeText(context, notificationMessage, Toast.LENGTH_LONG).show()
}

fun showToast(context: Context?, msg: String) {
    if (context == null) return
    Toast.makeText(
        context,
        msg,
        Toast.LENGTH_LONG
    ).show()
}
