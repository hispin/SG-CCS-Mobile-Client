package com.sensoguard.ccsmobileclient.activities

//import com.crashlytics.android.Crashlytics
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.MyExceptionHandler
import com.sensoguard.ccsmobileclient.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.ccsmobileclient.global.ToastNotify
import com.sensoguard.ccsmobileclient.services.MyFirebaseMessagingService
import com.sensoguard.ccsmobileclient.services.RegistrationIntentService
import com.sensoguard.ccsmobileclient.services.ServiceHandleAlarms
import timber.log.Timber

//import io.fabric.sdk.android.Fabric


class MainActivity : ParentActivity() {

    private val TAG: String = "MainActivity"

    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    //private var clickConsSensorTable: ConstraintLayout? = null
    private var clickConsMap: ConstraintLayout? = null
    private var clickConsConfiguration: ConstraintLayout? = null
    private var clickAlarmLog: ConstraintLayout? = null
    private var tvShowVer: TextView? = null
    //private var btnTest: AppCompatButton? = null

    fun registerWithNotificationHubs() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            val intent = Intent(this, RegistrationIntentService::class.java)
            //intent.putExtra("myTag",myTag)
            startService(intent)
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
                Timber.i("This device is not supported by Google Play Services.")
                ToastNotify("This device is not supported by Google Play Services.", this)
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

        //send context for accept message in future
        MyFirebaseMessagingService.createChannelAndHandleNotifications(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        configureGeneralCatch()

        //Fabric.with(this, Crashlytics())

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //hide unwanted badge of app icon
        hideBudgetNotification()

        initViews()
        //setOnClickSensorTable()
        setOnClickMapTable()
        setOnClickConfigTable()
        setOnClickAlarmLogTable()

        //hide status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //for testing
        //saveMyAccount()
        //start service for listening to alarms
        startListenerToAlarms()
    }

    /**
     * start service for listening to alarms
     */
    private fun startListenerToAlarms() {
        //start listener to alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ServiceHandleAlarms::class.java))
        } else {
            startService(Intent(this, ServiceHandleAlarms::class.java))
        }
    }

    //for testing :save locally the Email account details
//    private fun saveMyAccount() {
//        setStringInPreference(this, USER_NAME_MAIL, "sg-patrol@sgsmtp.com")
//        setStringInPreference(this, PASSWORD_MAIL, "SensoGuard1234")
//        setStringInPreference(this, SERVER_MAIL, "mail.sgsmtp.com")
//        setIntInPreference(this, PORT_MAIL, 587)
//        setStringInPreference(this, RECIPIENT_MAIL, "hag.swead@gmail.com")
//        setBooleanInPreference(this, IS_SSL_MAIL, false)
//    }


    //hide unwanted badge of app icon (icon)
    private fun hideBudgetNotification() {
        val id = "my_channel_01"
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel =
                NotificationChannel(id, name, importance).apply {
                    description = descriptionText
                    setShowBadge(false)
                }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        } else {

        }

    }
//    override fun onBackPressed() {
//
//        //showConformDialog()
//    }

    //show confirm dialog before stop usb process
//    private fun showConformDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(resources.getString(R.string.disconnect_usb))
//        val yes = resources.getString(R.string.yes)
//        val no = resources.getString(R.string.no)
//        builder.setMessage(resources.getString(R.string.this_will_disconnect_the_usb))
//            .setCancelable(false)
//        builder.setPositiveButton(yes) { dialog, which ->
//
//            super.onBackPressed()
//            //disconnect usb device and stop the process
//            //sendBroadcast(Intent(DISCONNECT_USB_PROCESS_KEY))
//            setBooleanInPreference(this@MainActivity, USB_DEVICE_CONNECT_STATUS, false)
//            sendBroadcast(Intent(DISCONNECT_USB_PROCESS_KEY))
//            //sendBroadcast(Intent(STOP_GENERAL_TIMER))
//
//            dialog.dismiss()
//
//        }
//
//
//        // Display a negative button on alert dialog
//        builder.setNegativeButton(no) { dialog, which ->
//            dialog.dismiss()
//        }
//        val alert = builder.create()
//        alert.show()
//    }

    private fun configureGeneralCatch() {
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
    }

    //    private fun setOnClickSensorTable() {
//        clickConsSensorTable?.setOnClickListener {
//            val inn = Intent(this, MyScreensActivity::class.java)
//            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)
//            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            startActivity(inn)
//        }
//    }
    private fun setOnClickMapTable() {
        clickConsMap?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,0)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickConfigTable() {
        clickConsConfiguration?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 2)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickAlarmLogTable() {
        clickAlarmLog?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 1)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }

    private fun initViews() {
        //clickConsSensorTable = findViewById(com.sensoguard.ccsmobileclient.R.id.clickConsSensorTable)
        clickConsMap = findViewById(R.id.clickConsMap)
        clickConsConfiguration =
            findViewById(R.id.clickConsConfiguration)
        clickAlarmLog = findViewById(R.id.clickAlarmLog)
        tvShowVer = findViewById(R.id.tvShowVer)

    }

    //Change View of fragment
    private fun replaceFragment(
        resId: Int,
        fragment: Fragment,
        add_to_back_stack: Boolean,
        tag: String
    ) {
        try {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            if (add_to_back_stack) {
                fragmentTransaction.addToBackStack(fragment.tag)
            }

            fragmentTransaction.replace(resId, fragment, tag)

            //fragmentTransaction.show(fragment)
            fragmentTransaction.commit()
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }

    }

}
