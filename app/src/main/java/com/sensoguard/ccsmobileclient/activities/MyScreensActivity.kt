package com.sensoguard.ccsmobileclient.activities

//import com.sensoguard.ccsmobileclient.services.ServiceConnectSensor
import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu
import com.sensoguard.ccsmobileclient.controler.ViewModelListener
import com.sensoguard.ccsmobileclient.fragments.AlarmsLogFragment
import com.sensoguard.ccsmobileclient.fragments.ConfigurationFragment
import com.sensoguard.ccsmobileclient.fragments.MapmobFragment
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.ccsmobileclient.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.MAIN_MENU_NUM_ITEM
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.sensoguard.ccsmobileclient.global.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
import com.sensoguard.ccsmobileclient.global.REGISTER_TOKEN_STATUS
import com.sensoguard.ccsmobileclient.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
import com.sensoguard.ccsmobileclient.global.TOKEN_REGISTRATION_STATUS_KEY
import com.sensoguard.ccsmobileclient.global.USB_DEVICES_EMPTY
import com.sensoguard.ccsmobileclient.global.USB_DEVICES_NOT_EMPTY
import com.sensoguard.ccsmobileclient.global.USB_DEVICE_CONNECT_STATUS
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setAppLanguage
import com.sensoguard.ccsmobileclient.global.setIntInPreference
import com.sensoguard.ccsmobileclient.global.setLongInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.interfaces.OnFragmentListener
import com.sensoguard.ccsmobileclient.services.MediaService
import java.util.*


class MyScreensActivity : ParentActivity(), OnFragmentListener, Observer {


    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager
    private var currentItemTopMenu = 0
    private var consMyActionBar: ConstraintLayout? = null


    val TAG = "MyScreensActivity"


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        startTimerListener()

        setContentView(R.layout.activity_my_screens)

        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLocationPermission()
        } else {
            init()
        }


    }

    //start listener to timer
    private fun startTimerListener() {
        //this?.let {
        ViewModelProviders.of(this).get(ViewModelListener::class.java)
            .startCurrentCalendarListener()?.observe(this, androidx.lifecycle.Observer { calendar ->

                //if there is no alarm in process then shut down the timer
                if (UserSession.instance.alarmSensors == null
                    || UserSession.instance.alarmSensors?.isEmpty()!!
                    || isAllSensorAlarmTimeOutSound()
                ) {
                    ViewModelProviders.of(this).get(ViewModelListener::class.java).shutDownTimer()
                    sendBroadcast(Intent(STOP_ALARM_SOUND))
                }

            })
    }

    /**
     * check if all the alarms are timeout for sound only
     */
    private fun isAllSensorAlarmTimeOutSound(): Boolean {
        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (sensorItem.isSound) {
                return false
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (isAnySensorAlarmNotTimeOut()) {
            startTimer()
        }
        configureActionBar()
    }


    //create timeout for reset sensor to regular icon and cancel the alarm icon
    private fun startTimer() {

        Log.d("testTimer", "start timer")
        ViewModelProviders.of(this).get(ViewModelListener::class.java).startTimer()


    }

    //check it there is any sensor alarm which is not time out
    private fun isAnySensorAlarmNotTimeOut(): Boolean {
        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (!isSensorAlarmTimeout(sensorItem)) {
                return true
            }
        }
        return false
    }

    //check if the alarm sensor is in duration
    private fun isSensorAlarmTimeout(alarmProcess: AlarmSensor?): Boolean {

        val timeout = getLongInPreference(
            this,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        val futureTimeout = timeout?.let { alarmProcess?.alarmTime?.timeInMillis?.plus(it * 1000) }

        if (futureTimeout != null) {
            val calendar = Calendar.getInstance()
            return when {
                futureTimeout < calendar.timeInMillis -> true
                //alarmProcess.marker.setIcon(context?.let { con -> convertBitmapToBitmapDiscriptor(con,R.drawable.ic_sensor_item) })
                //alarmSensors?.remove(alarmProcess)
                else -> false
            }
        }
        return true
    }

    //store locally default values of configuration
    private fun setConfigurationDefault() {

        if (getLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, -1L) == -1L) {
            //set the duration of flickering icon when accepted alarm
            setLongInPreference(
                this,
                ALARM_FLICKERING_DURATION_KEY,
                ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
            )
        }

        if (getIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, -1) == -1) {
            //set the type of map
            setIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
        }

        if (getStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, "-1").equals("-1")) {

            val uri=Uri.parse("android.resource://$packageName/raw/alarm_sound")

            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, arg1: Intent) {
            Log.d("testAlarmMap", "MyScreenActivity:arg1.action" + arg1.action)
            when {
                arg1.action == USB_DEVICES_EMPTY -> {
                    setUIusbConnection(false)

                }
                arg1.action == USB_DEVICES_NOT_EMPTY -> {
                    val isConnected =
                        getBooleanInPreference(context, USB_DEVICE_CONNECT_STATUS, false)
                    if (isConnected) {
                        setUIusbConnection(true)
                    } else {
                        //if there is device and the status is wrong then restart the connection
                        //startConnectionService()
                    }

                }
                arg1.action == UsbManager.ACTION_USB_DEVICE_ATTACHED -> {

                }
                //when disconnect the device from USB
                arg1.action == UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    showUsbReadDisconnection()
                    playVibrate()
                    showDisconnectUsbDialog()
                }
                arg1.action == CREATE_ALARM_KEY -> {
                    Log.d("testAlarmMap", "MyScreenActivity:startTimer")
                    startTimer()
                }
                arg1.action == STOP_ALARM_SOUND -> {

                }
                arg1.action == "not_connection" -> {
                    //showToast(this@MyScreensActivity,"not_connection")
                }
                arg1.action == "yes_connection" -> {
                    //showToast(this@MyScreensActivity,"yes_connection")
                }
                arg1.action == TOKEN_REGISTRATION_STATUS_KEY -> {
                    //if the token registration status
                    //val isConnected = getBooleanInPreference(context, REGISTER_TOKEN_STATUS, false)
                }


            }
        }

        //show dialog with disconnect usb message
        private fun showDisconnectUsbDialog() {
            val builder = AlertDialog.Builder(this@MyScreensActivity)
            builder.setIcon(R.drawable.ic_alert)
            builder.setTitle(resources.getString(R.string.receiver_disconnected))
                .setCancelable(false)
            val ok = resources.getString(R.string.OK)

            builder.setPositiveButton(ok) { dialog, which ->

                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }

        //execute vibrate
        private fun playVibrate() {

            val isVibrateWhenAlarm =
                getBooleanInPreference(applicationContext, IS_VIBRATE_WHEN_ALARM_KEY, true)
            if (isVibrateWhenAlarm) {
                // Get instance of Vibrator from current Context
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                // Vibrate for 200 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            1000,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    vibrator.vibrate(1000)
                }

            }

        }
    }


    //show usb read disconnection
    private fun showUsbReadDisconnection() {
        //bug fixed : kill also the service
        setUIusbConnection(false)
    }

    private fun setFilter() {
        val filter = IntentFilter(USB_DEVICES_EMPTY)
        //filter.addAction("android.hardware.usb.action.USB_STATE")
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(USB_DEVICES_NOT_EMPTY)
        filter.addAction(CREATE_ALARM_KEY)
        filter.addAction(STOP_ALARM_SOUND)
        filter.addAction("not_connection")
        filter.addAction("yes_connection")
        filter.addAction(TOKEN_REGISTRATION_STATUS_KEY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }


    private fun init() {

        configureActionBar()

        configTabs()

    }

    private fun setUIusbConnection(state: Boolean) {
    }

    //TODO : the toggle will updated by the status changing
    private fun configureActionBar() {

        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)//supportActionBar
        setSupportActionBar(toolbar)


        consMyActionBar = findViewById(
            R.id.consMyActionBar
        )

        //if the token registration status
        val isConnected = getBooleanInPreference(this, REGISTER_TOKEN_STATUS, false)
    }

    //start connection service
//    private fun startConnectionService() {
//
//        val connector = Intent(this, ServiceConnectSensor::class.java)
//        //Toast.makeText(this,"isConnected2="+isConnected, Toast.LENGTH_SHORT).show()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(connector)
//        } else {
//            startService(Intent(connector))
//        }
//    }


    private fun configTabs() {

        val tabs = findViewById<TabLayout>(R.id.tab_layout)

        viewPager = findViewById(R.id.vPager)

        collectionPagerAdapter = CollectionPagerAdapter(supportFragmentManager)
        viewPager.adapter = collectionPagerAdapter
        viewPager.offscreenPageLimit = 0
        //prevent change screen by drag
        viewPager.setOnTouchListener(object : OnTouchListener {


            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return true
            }
        })

        //relate the tab layout to viewpager because we need to add the icons
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.icon = ContextCompat.getDrawable(
            this@MyScreensActivity,
            R.drawable.selected_map_tab
        )
        tabs.getTabAt(1)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_alarm_log_tab)
        tabs.getTabAt(2)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_config_tab)


        viewPager.currentItem = currentItemTopMenu


    }


    override fun onStart() {
        super.onStart()
        setFilter()
        val serviceIntent = Intent(this, MediaService::class.java)
        stopService(serviceIntent)
    }


    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(usbReceiver)
        } catch (ex: Exception) {

        }
    }


    private fun setLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init()
            //setExternalPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                init()
                //setExternalPermission()
            }
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                }
            }
        }

    }


    private fun setExternalPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */

        val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init()
        } else {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    inner class CollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getCount(): Int = MAIN_MENU_NUM_ITEM

        override fun getItem(position: Int): Fragment {

            var fragment: Fragment? = null
            //set event of click ic_on top menu
            when (position) {
                0 -> {
                    fragment = MapmobFragment()//MapSensorsFragment()//MapmobFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                2 -> {
                    fragment = ConfigurationFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                1 -> {
                    fragment = AlarmsLogFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
            }
            return fragment!!

        }

        override fun getPageTitle(position: Int): CharSequence {

            //set the title text of top menu
            return when (position) {
                //0 -> resources.getString(R.string.sensor_table_title)
                0 -> resources.getString(R.string.map_title)
                1 -> resources.getString(R.string.alarm_log_title)
                2 -> resources.getString(R.string.config_title)
                else -> "nothing"
            }

        }

    }


    override fun onBackPressed() {
        //back press when the command fragment is showed
        val prev = supportFragmentManager.findFragmentByTag("CommandsFragment")
        if (prev != null && prev.isAdded) {
            val df: DialogFragment = prev as DialogFragment
            df.dismiss()

        } else {//normal
            super.onBackPressed()
            sendBroadcast(Intent(STOP_ALARM_SOUND))
            //start activity for loading new language if it has been changed
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        intent.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 3)
        this.startActivity(intent)
    }


    override fun update(o: Observable?, arg: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}






