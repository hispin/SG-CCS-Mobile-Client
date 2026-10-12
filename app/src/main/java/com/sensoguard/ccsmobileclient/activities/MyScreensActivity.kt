package com.sensoguard.ccsmobileclient.activities

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
import com.sensoguard.ccsmobileclient.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
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
import java.util.Calendar


class MyScreensActivity : ParentActivity(), OnFragmentListener {

    private lateinit var viewPager: ViewPager2
    private var currentItemTopMenu = 0

    //show the screens also when the permission is denied
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            init()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        startTimerListener()

        setContentView(R.layout.activity_my_screens)

        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        setLocationPermission()
    }

    private val viewModelListener: ViewModelListener
        get() = ViewModelProvider(this)[ViewModelListener::class.java]

    //start listener to timer
    private fun startTimerListener() {
        viewModelListener.startCurrentCalendarListener().observe(this) {
            //if there is no alarm in process then shut down the timer
            if (UserSession.instance.alarmSensors.isEmpty()
                || isAllSensorAlarmTimeOutSound()
            ) {
                viewModelListener.shutDownTimer()
                sendBroadcast(Intent(STOP_ALARM_SOUND).setPackage(packageName))
            }
        }
    }

    /**
     * check if all the alarms are timeout for sound only
     */
    private fun isAllSensorAlarmTimeOutSound(): Boolean {
        return UserSession.instance.alarmSensors.none { it.isSound }
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
        Log.d(TAG, "start timer")
        viewModelListener.startTimer()
    }

    //check it there is any sensor alarm which is not time out
    private fun isAnySensorAlarmNotTimeOut(): Boolean {
        return UserSession.instance.alarmSensors.any { !isSensorAlarmTimeout(it) }
    }

    //check if the alarm sensor is in duration
    private fun isSensorAlarmTimeout(alarmProcess: AlarmSensor): Boolean {
        val timeout = getLongInPreference(
            this,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        ) ?: return true
        val futureTimeout = alarmProcess.alarmTime.timeInMillis + timeout * 1000
        return futureTimeout < Calendar.getInstance().timeInMillis
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
            //the default alarm sound
            val uri = "android.resource://$packageName/${R.raw.alarm_sound}"
            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri)
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, arg1: Intent) {
            Log.d(TAG, "action: " + arg1.action)
            when (arg1.action) {
                //when disconnect the device from USB
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    playVibrate()
                    showDisconnectUsbDialog()
                }

                CREATE_ALARM_KEY -> startTimer()
            }
        }

        //show dialog with disconnect usb message
        private fun showDisconnectUsbDialog() {
            val builder = AlertDialog.Builder(this@MyScreensActivity)
            builder.setIcon(R.drawable.ic_alert)
            builder.setTitle(resources.getString(R.string.receiver_disconnected))
                .setCancelable(false)
            val ok = resources.getString(R.string.OK)

            builder.setPositiveButton(ok) { dialog, _ ->
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
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }

                // Vibrate for 1 second
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }
    }

    private fun setFilter() {
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(CREATE_ALARM_KEY)
        ContextCompat.registerReceiver(
            this,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    private fun init() {
        configureActionBar()
        configTabs()
    }

    private fun configureActionBar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun configTabs() {

        val tabs = findViewById<TabLayout>(R.id.tab_layout)

        viewPager = findViewById(R.id.vPager)
        viewPager.adapter = CollectionPagerAdapter(this)
        //prevent change screen by drag
        viewPager.isUserInputEnabled = false

        //relate the tab layout to viewpager (set the titles and the icons)
        TabLayoutMediator(tabs, viewPager, false, false) { tab, position ->
            tab.text = getPageTitle(position)
            tab.icon = ContextCompat.getDrawable(this, getPageIcon(position))
        }.attach()

        viewPager.setCurrentItem(currentItemTopMenu, false)
    }

    //the title text of top menu
    private fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> resources.getString(R.string.map_title)
            1 -> resources.getString(R.string.alarm_log_title)
            else -> resources.getString(R.string.config_title)
        }
    }

    //the icon of top menu
    private fun getPageIcon(position: Int): Int {
        return when (position) {
            0 -> R.drawable.selected_map_tab
            1 -> R.drawable.selected_alarm_log_tab
            else -> R.drawable.selected_config_tab
        }
    }


    override fun onStart() {
        super.onStart()
        setFilter()
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(usbReceiver)
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
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    //the screens of the tabs
    private class CollectionPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = MAIN_MENU_NUM_ITEM

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MapmobFragment()
                1 -> AlarmsLogFragment()
                else -> ConfigurationFragment()
            }
        }
    }


    //handles both the back button and the back gesture (onBackPressed is not called for gestures)
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            sendBroadcast(Intent(STOP_ALARM_SOUND).setPackage(packageName))
            //start activity for loading new language if it has been changed
            startActivity(Intent(this@MyScreensActivity, MainActivity::class.java))
            finish()
        }
    }

    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        //return to the configuration screen
        intent.putExtra(CURRENT_ITEM_TOP_MENU_KEY, CONFIGURATION_SCREEN_INDEX)
        this.startActivity(intent)
    }

    companion object {
        private const val TAG = "MyScreensActivity"
        private const val CONFIGURATION_SCREEN_INDEX = 2
    }
}
