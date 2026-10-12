package com.sensoguard.ccsmobileclient.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.global.CURRENT_LOCATION
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_SINGLE_LOCATION_KEY

/**
 * get the current location once and send it to the screens
 * (start it only when the location permission is granted, see [hasLocationPermission])
 */
class ServiceFindSingleLocation : Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val inn = Intent(GET_CURRENT_SINGLE_LOCATION_KEY)
                inn.putExtra(CURRENT_LOCATION, location)
                sendBroadcast(inn.setPackage(packageName))
                stopSelf()
            }
        }
    }


    //the service is started and not bound
    override fun onBind(intent: Intent?): IBinder? = null


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSysForeGround()
        startGetLocation()
        return START_NOT_STICKY
    }

    private fun startGetLocation() {
        if (!hasLocationPermission(this)) {
            stopSelf()
            return
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdates(1)
            .build()
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            //the permission has been revoked
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        val channel = NotificationChannel(
            ServiceHandleAlarms.CHANNEL_ID,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, ServiceHandleAlarms.CHANNEL_ID)
            .setContentText("SG-CCS Mobile Client is running")
            .setSmallIcon(R.drawable.ic_app_notification)
            .build()

        startForeground(1, notification)
    }

    companion object {
        /**
         * a foreground service of location can be started only with the location permission
         */
        fun hasLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
