package com.sensoguard.ccsmobileclient.global

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.sensoguard.ccsmobileclient.R

private const val TAG = "SysFcm"

/**
 * get the token of firebase messaging and store it locally (it can be shared from the configuration screen)
 */
fun registerFcmToken(context: Context) {
    val appContext = context.applicationContext
    //getToken is deprecated in firebase-messaging 26, it is kept until the new registration api is documented
    @Suppress("DEPRECATION")
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        val token = if (task.isSuccessful) task.result else null
        if (token != null) {
            storeFcmToken(appContext, token)
        } else {
            Log.e(TAG, "failed to get the token", task.exception)
            Toast.makeText(
                appContext,
                appContext.getString(R.string.failed_registration),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

//store the token locally
fun storeFcmToken(context: Context, token: String) {
    setStringInPreference(context, FCM_TOKEN_KEY, token)
}
