package com.sensoguard.ccsmobileclient.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.sensoguard.ccsmobileclient.global.configurationLanguage

open class ParentService : Service() {

    //the services are started and not bound
    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        configurationLanguage(applicationContext)
        return super.onStartCommand(intent, flags, startId)
    }
}
