package com.sensoguard.ccsmobileclient.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.MyExceptionHandler
import com.sensoguard.ccsmobileclient.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.ccsmobileclient.global.registerFcmToken
import com.sensoguard.ccsmobileclient.global.toastNotify
import com.sensoguard.ccsmobileclient.services.MyFirebaseMessagingService
import com.sensoguard.ccsmobileclient.services.ServiceHandleAlarms


class MainActivity : ParentActivity() {

    private var clickConsMap: ConstraintLayout? = null
    private var clickConsConfiguration: ConstraintLayout? = null
    private var clickAlarmLog: ConstraintLayout? = null
    private var tvShowVer: TextView? = null

    private fun registerWithNotificationHubs() {
        if (checkPlayServices()) {
            //register this application with FCM
            registerFcmToken(this)
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {

        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                    ?.show()
            } else {
                toastNotify(resources.getString(R.string.play_services_not_supported), this)
                finish()
            }
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        //show version name
        val verName = packageManager.getPackageInfo(packageName, 0).versionName
        val verTitle = "version:$verName"
        tvShowVer?.text = verTitle

        registerWithNotificationHubs()

        //create the channel of the notifications of the alarms
        MyFirebaseMessagingService.createChannelAndHandleNotifications(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        configureGeneralCatch()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //hide unwanted badge of app icon
        hideBudgetNotification()

        initViews()
        setOnClickMapTable()
        setOnClickConfigTable()
        setOnClickAlarmLogTable()

        //hide status bar
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        //keep the content above the navigation bar (the app is drawn edge to edge)
        val content = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }

        //start service for listening to alarms
        startListenerToAlarms()
    }

    /**
     * start service for listening to alarms
     */
    private fun startListenerToAlarms() {
        startForegroundService(Intent(this, ServiceHandleAlarms::class.java))
    }


    //hide unwanted badge of app icon (icon)
    private fun hideBudgetNotification() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val mChannel =
            NotificationChannel(
                ServiceHandleAlarms.CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = descriptionText
                setShowBadge(false)
            }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun configureGeneralCatch() {
        //set the handler only once (it keeps the previous handler)
        if (Thread.getDefaultUncaughtExceptionHandler() !is MyExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
        }
    }

    private fun openScreen(screenIndex: Int) {
        val inn = Intent(this, MyScreensActivity::class.java)
        inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, screenIndex)
        inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(inn)
    }

    private fun setOnClickMapTable() {
        clickConsMap?.setOnClickListener {
            openScreen(0)
        }
    }

    private fun setOnClickConfigTable() {
        clickConsConfiguration?.setOnClickListener {
            openScreen(2)
        }
    }

    private fun setOnClickAlarmLogTable() {
        clickAlarmLog?.setOnClickListener {
            openScreen(1)
        }
    }

    private fun initViews() {
        clickConsMap = findViewById(R.id.clickConsMap)
        clickConsConfiguration = findViewById(R.id.clickConsConfiguration)
        clickAlarmLog = findViewById(R.id.clickAlarmLog)
        tvShowVer = findViewById(R.id.tvShowVer)
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }
}
