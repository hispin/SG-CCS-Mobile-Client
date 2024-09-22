package com.sensoguard.ccsmobileclient.fragments

import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textVariableAnchor
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.activities.MainActivity
import com.sensoguard.ccsmobileclient.activities.MyScreensActivity
import com.sensoguard.ccsmobileclient.adapters.SensorsDialogAdapter
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.classes.Sensor
import com.sensoguard.ccsmobileclient.controler.ViewModelListener
import com.sensoguard.ccsmobileclient.global.ACTION_TOGGLE_TEST_MODE
import com.sensoguard.ccsmobileclient.global.ALARM_CAR
import com.sensoguard.ccsmobileclient.global.ALARM_CAR_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DIGGING
import com.sensoguard.ccsmobileclient.global.ALARM_DIGGING_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNCTED
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNCTED_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH_STR
import com.sensoguard.ccsmobileclient.global.ALARM_EXTERNAL
import com.sensoguard.ccsmobileclient.global.ALARM_EXTERNAL_STR
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.ALARM_FOOTSTEPS
import com.sensoguard.ccsmobileclient.global.ALARM_FOOTSTEPS_STR
import com.sensoguard.ccsmobileclient.global.ALARM_GATEWAY_DISCONNECTED
import com.sensoguard.ccsmobileclient.global.ALARM_GATEWAY_DISCONNECTED_STR
import com.sensoguard.ccsmobileclient.global.ALARM_KEEP_ALIVE
import com.sensoguard.ccsmobileclient.global.ALARM_KEEP_ALIVE_STR
import com.sensoguard.ccsmobileclient.global.ALARM_LOW_BATTERY
import com.sensoguard.ccsmobileclient.global.ALARM_LOW_BATTERY_STR
import com.sensoguard.ccsmobileclient.global.ALARM_TYPE_INDEX_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_ID_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_IS_ARMED
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_NAME_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_INDEX_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_LATITUDE_PREF
import com.sensoguard.ccsmobileclient.global.CURRENT_LOCATION
import com.sensoguard.ccsmobileclient.global.CURRENT_LONGTUDE_PREF
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_LOCATION_KEY
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_SINGLE_LOCATION_KEY
import com.sensoguard.ccsmobileclient.global.IS_SENSOR_NAME_ALWAYS_KEY
import com.sensoguard.ccsmobileclient.global.LAST_LATITUDE
import com.sensoguard.ccsmobileclient.global.LAST_LONGETITUDE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.RESET_MARKERS_KEY
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
import com.sensoguard.ccsmobileclient.global.TABLAYOUT_HEIGHT_DEFAULT
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.addAlarmToQueue
import com.sensoguard.ccsmobileclient.global.dpToPx
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getDoubleInPreference
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import com.sensoguard.ccsmobileclient.global.getSensorsFromLocally
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.global.storeSensorsToLocally
import com.sensoguard.ccsmobileclient.interfaces.OnAdapterListener
import com.sensoguard.ccsmobileclient.services.MediaService
import com.sensoguard.ccsmobileclient.services.MyFirebaseMessagingService
import com.sensoguard.ccsmobileclient.services.ServiceFindLocation
import com.sensoguard.ccsmobileclient.services.ServiceFindSingleLocation
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val CHANNEL_NAME = "newAlarmDetected"
const val CHANNEL_ID = "1.0"

/**
 * A simple [Fragment] subclass.
 * Use the [MapmobFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapmobFragment : ParentFragment(), OnAdapterListener, MapboxMap.OnMoveListener {
    private var popup: PopupWindow? = null
    private var currentLocationMarker: Feature? = null
    private var markersList: ArrayList<Feature>? = null
    private var symbolOption: SymbolOptions? = null
    private var markerViewManager: MarkerViewManager? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mapView: MapView? = null
    private var mapType = Style.OUTDOORS
    private var myLocate: LatLng? = null
    private var loadedMapStyle: Style? = null

    private var fbRefresh: FloatingActionButton? = null
    private var fbTest: FloatingActionButton? = null
    private var fbClear: FloatingActionButton? = null

    //private var btnDownloadMaps:AppCompatButton?=null

    private var myMapboxMap: MapboxMap? = null
    private var myOfflineRegion: OfflineRegion? = null

    private var currentLongitude: Double? = null
    private var currentLatitude: Double? = null
    private var mCenterLatLong: LatLng? = null

    val TAG = "MapmobFragment"

    private var dialog: Dialog? = null
    var sensorsDialogAdapter: SensorsDialogAdapter? = null

    private val SOURCE_ID = "SOURCE_ID"
    private val CURRENT_LOC_SOURCE = "current_loc_source"
    private val LAYER_ID = "LAYER_ID"

    private val ICON_PROPERTY: String = "ICON_PROPERTY"
    private val BLUE_ICON_ID = "BLUE_ICON_ID"
    private val GREEN_ICON_ID = "GREEN_ICON_ID"
    private val GRAY_ICON_ID = "GRAY_ICON_ID"
    private val RED_ICON_ID = "RED_ICON_ID"
    private val CAR_ICON_ID = "CAR_ICON_ID"
    private val INTRUDER_ICON_ID = "INTRUDER_ICON_ID"
    private val SENSOR_OFF_ICON_ID = "SENSOR_OFF_ICON_ID"
    private val PIR_ICON_ID = "PIR_ICON_ID"
    private val RADAR_ICON_ID = "RADAR_ICON_ID"
    private val VIBRATION_ICON_ID = "VIBRATION_ICON_ID"
    private val ZOMM_LEVEL = 5.0

    private var locationManager: LocationManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        startTimerListener()
    }

    //start listener to timer
    private fun startTimerListener() {
        activity?.let {
            ViewModelProviders.of(it).get(ViewModelListener::class.java)
                .startCurrentCalendarListener()?.observe(
                    this,
                    { calendar ->
                        Log.d("testAlarmMap", "startTimerListener in MapSensorsFragment")
                        //Log.d("testTimer","tick in MapSensorsFragment")
                        //if there is no alarm in process then shut down the timer
                        if (UserSession.instance.alarmSensors == null
                            || UserSession.instance.alarmSensors?.isEmpty()!!
                            || isAllSensorAlarmTimeOutSound()
                        ) {

                            activity?.let { act ->
                                ViewModelProviders.of(act).get(ViewModelListener::class.java)
                                    .shutDownTimer()
                            }
                            //showMarkers()
                        } else {
                            //set all the time out sensors alarm as sound off
                            replaceSensorAlarmTimeOutToSensorMarker()
//                            showMarkers()
                        }
                        //if the
                        Log.d(
                            "testAlarmMap",
                            "startTimerListener in MapSensorsFragment:showMarkers"
                        )
                        showMarkers()

                    })

        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        val view = inflater.inflate(R.layout.fragment_mapmob, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)


        fbRefresh = view.findViewById(R.id.fbRefresh1)
        fbRefresh?.setOnClickListener {
            gotoMySingleLocation()
        }

        fbClear = view.findViewById(R.id.fbClear)
        fbClear?.setOnClickListener {
            clearAlarms()
        }

        fbTest = view.findViewById(R.id.fbTest1)
        fbTest?.setOnClickListener {
            showTestEventDialog()
        }


        initMapType()

        return view
    }

    private fun clearAlarms() {
        UserSession.instance.alarmSensors = ArrayList()
        removeMarker()
        showMarkers()
    }

    /**
     * remove marker with details
     */
    private fun removeMarker() {
        if (popup != null)
            popup?.dismiss()
    }

    //get last location
    private fun initFindLocation(): Location? {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {

            return locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }


        return null
    }

    // move the camera to ic_mark location
    private fun showLocation(location: Location?) {

        if (location != null) {
            setMyLocate(
                LatLng(
                    location.latitude,
                    location.longitude
                )
            )
        } else {

            myLocate = getLastLocationLocally()

            if (myLocate == null) {
                //set default location (london)
                myLocate = LatLng(51.509865, -0.118092)
                //set default location (london) if there is no last location
                setMyLocate(LatLng(51.509865, -0.118092))
            }
        }
        //add marker at the focus of the map
        myLocate?.let {
            //load the camera
            if (myLocate != null && myLocate?.latitude != null &&
                myLocate?.longitude != null
            ) {

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(myLocate?.latitude!!, myLocate?.longitude!!))
                    .zoom(ZOMM_LEVEL)
                    .tilt(20.0)
                    .build()


                if (myMapboxMap != null) {
                    // Move camera to new position
                    myMapboxMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }

                showMarkers()

            }

        }

    }

    /**
     * show all markers
     */
    fun showMarkers() {

        //clear the markers
        markersList = ArrayList<Feature>()

        //show current location marker
        showCurrentLocationMarker()


        val alarmSensor = UserSession.instance.alarmSensors
        val iteratorList = alarmSensor?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (sensorItem.latitude != null
                && sensorItem.longitude != null
            ) {
                if (sensorItem != null) {

                    //if time out then remove the sensor from alarm list
//                    if (isSensorAlarmTimeout(sensorItem)) {
//                        iteratorList.remove()
//                    } else {
                    //save the marker for update after timeout
                    sensorItem.markerFeature = showSensorAlarmMarker(
                        sensorItem,
                        sensorItem.type,
                        sensorItem.typeIdx,
                        sensorItem.zone
                    )
//                    }

                } else {
                    //show sensor marker
                    showSensorMarker(sensorItem)
                }
            }
        }
    }




    //get current location from gps
    private fun gotoMySingleLocation() {
        //just when there is no alarms go to current location
        if (UserSession.instance.alarmSensors == null
            || UserSession.instance.alarmSensors?.isEmpty()!!
        ) {

            if (checkLastAlarm()) {
                showLocationLastAlarm()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(
                        Intent(
                            context,
                            ServiceFindSingleLocation::class.java
                        )
                    )
                } else {
                    activity?.startService(Intent(context, ServiceFindSingleLocation::class.java))
                }
            }
        }
    }

    /**
     * show location by location of the last alarm
     *
     */
    private fun showLocationLastAlarm() {
        val location = Location("last alarm")
        if (latitude == null || longitude == null)
            return

        location.latitude = latitude!!
        location.longitude = longitude!!

        if (location != null) {
            //save locally the current location
            setStringInPreference(
                activity,
                CURRENT_LATITUDE_PREF,
                location.latitude.toString()
            )
            setStringInPreference(
                activity,
                CURRENT_LONGTUDE_PREF,
                location.longitude.toString()
            )
            showLocation(location)
        } else {
            Toast.makeText(activity, "error in location2", Toast.LENGTH_LONG).show()
        }
    }

    var latitude: Double? = null
    var longitude: Double? = null

    /**
     * check if the last location is saved
     */
    private fun checkLastAlarm(): Boolean {

        latitude = getDoubleInPreference(requireContext(), LAST_LATITUDE, -1.0)
        longitude = getDoubleInPreference(requireContext(), LAST_LONGETITUDE, -1.0)

        return latitude != -1.0 && longitude != -1.0
    }

    //for test :enter manually alarm
    private fun showTestEventDialog() {

        startServiceMedia()

        Log.d("testTest", "showTestEventDialog")

        addAlarmToQueue("GSM_Dani-GSM_Unit1", 34.0, 34.0, "Footsteps", true, 0, "")

        sendNotification("test")
        //sendNotification()

        //fun sendAlarm(alarmId: String, typeAlarm: String, lat: Double, lon: Double) {
        val inn = Intent(CREATE_ALARM_KEY)
        inn.putExtra(CREATE_ALARM_ID_KEY, "GSM_Dani-GSM_Unit1")
        inn.putExtra(CREATE_ALARM_NAME_KEY, "GSM_Dani-GSM_Unit1")
        inn.putExtra(CREATE_ALARM_IS_ARMED, true)
        inn.putExtra(CREATE_ALARM_TYPE_KEY, "Footsteps")
        inn.putExtra(ALARM_TYPE_INDEX_KEY, -1)
        requireActivity().sendBroadcast(inn)
    }

    //start service media
    private fun startServiceMedia() {
        val serviceIntent = Intent(requireActivity(), MediaService::class.java)
        ContextCompat.startForegroundService(requireActivity(), serviceIntent)
    }

    private var mNotificationManager: NotificationManager? = null

    ///////////////////////////////


    //    Notification channels enable us app developers to group our notifications into groups—channels—with
    //    the user having the ability to modify notification settings for the entire channel at once. For example,
    //    for each channel, users can completely block all notifications, override the importance level, or allow a
    //    notification badge to be shown. This new feature helps in greatly improving the user experience of an app
    private fun createNotificationChannel(context: Context?) {
        if (context == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: String = CHANNEL_NAME
            val descriptionText = "new alarm detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    //snd notification when accept alarm
    fun sendNotification() {
        //if (myIntent == null) return
        val intent = Intent(requireActivity(), MyScreensActivity::class.java)
        intent.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 2)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val title = "title"//Objects.requireNonNull(myIntent.extras).getString("title")
        val message = "message"//Objects.requireNonNull(myIntent.extras).getString("message")

        //add extras data that accepted from push
        //intent.putExtras(myIntent)
        mNotificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val oneTimeID = SystemClock.uptimeMillis()
        val contentIntent: PendingIntent
        contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //set different request code to make different extra for each notification
            PendingIntent.getActivity(
                requireActivity(), oneTimeID.toInt(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            //set different request code to make different extra for each notification
            PendingIntent.getActivity(
                requireActivity(), oneTimeID.toInt(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }


        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val notificationBuilder = NotificationCompat.Builder(
            requireActivity(),
            MyFirebaseMessagingService.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) //remove after click on notification
            .setAutoCancel(true)
        notificationBuilder.setContentIntent(contentIntent)

        //send
        mNotificationManager!!.notify(oneTimeID.toInt(), notificationBuilder.build())
    }
    //////////////////////////////////////

    private fun sendNotification(msg: String) {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mNotificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val oneTimeID = SystemClock.uptimeMillis()
        val contentIntent: PendingIntent
        //long oneTimeID = SystemClock.uptimeMillis();
        contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //set different request code to make different extra for each notification
            PendingIntent.getActivity(
                requireActivity(), oneTimeID.toInt(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            //set different request code to make different extra for each notification
            PendingIntent.getActivity(
                requireActivity(), oneTimeID.toInt(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }


        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val notificationBuilder = NotificationCompat.Builder(
            requireActivity(),
            MyFirebaseMessagingService.NOTIFICATION_CHANNEL_ID
        )
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) //remove after click on notification
            .setAutoCancel(true)
        notificationBuilder.setContentIntent(contentIntent)
        mNotificationManager!!.notify(oneTimeID.toInt(), notificationBuilder.build())
        //mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private fun validIsEmpty(editText: EditText): Boolean {
        var isValid = true

        if (editText.text.isNullOrBlank()) {
            editText.error =
                resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }

    /***
     * show marker of sensor
     * TODO temporary cancel this function
     */
    private fun showSensorMarker(sensorItem: Sensor) {

//        if (mapView == null || sensorItem == null) {
//            return
//        }
//
//        val loc = LatLng(
//            sensorItem.getLatitude()!!,
//            sensorItem.getLongtitude()!!
//        )
//
//
//        if (sensorItem.isArmed()) {
//
//            addMarker(
//                loc,
//                GREEN_ICON_ID,
//                sensorItem.getName(),
//                sensorItem.getType()
//            )
//
//        } else {
//            addMarker(
//                loc,
//                GRAY_ICON_ID,
//                sensorItem.getName(),
//                sensorItem.getType()
//            )
//
//        }
    }

    /***
     * show marker of sensor alarm
     */
    private fun showSensorAlarmMarker(
        sensorItem: AlarmSensor,
        type: String,
        typeIdx: Int?,
        zone: String
    ): Feature? {

        if (mapView == null) {
            return null
        }

        val loc: LatLng =
            LatLng(
                sensorItem.latitude!!,
                sensorItem.longitude!!
            )

        var alarmTypeIcon: Feature? = null

        alarmTypeIcon =
            when (typeIdx) {
                ALARM_CAR -> {
                    loc.let { addMarker(it, ALARM_CAR_STR, sensorItem.alarmSensorId, type, zone) }
                }
                ALARM_FOOTSTEPS -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_FOOTSTEPS_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_DIGGING -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_DIGGING_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_EXTERNAL -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_EXTERNAL_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_DISCONNCTED -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_DISCONNCTED_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_KEEP_ALIVE -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_KEEP_ALIVE_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_LOW_BATTERY -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_LOW_BATTERY_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_DUAL_TECH -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_DUAL_TECH_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                ALARM_GATEWAY_DISCONNECTED -> {
                    loc.let {
                        addMarker(
                            it,
                            ALARM_GATEWAY_DISCONNECTED_STR,
                            sensorItem.alarmSensorId,
                            type,
                            zone
                        )
                    }
                }
                //ALARM_LOW_BATTERY->context?.let { con -> convertBitmapToBitmapDiscriptor(con,R.drawable.ic_alarm_low_battery)}
                else -> {
                    loc.let { addMarker(it, RED_ICON_ID, sensorItem.alarmSensorId, type, zone) }
                }
            }


        moveCamera(loc)


        return alarmTypeIcon
    }

    /**
     * move the camera to location of alarm
     */
    private fun moveCamera(loc: LatLng?) {
        val cameraPosition = CameraPosition.Builder()
            .target(loc)
            .zoom(ZOMM_LEVEL)
            .tilt(20.0)
            .build()


        if (myMapboxMap != null) {
            // Move camera to new position
            myMapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
        }
    }

    /**
     * check if the alarm sensor is in duration
     */
    private fun isSensorAlarmTimeout(alarmProcess: AlarmSensor?): Boolean {

        val timeout = getLongInPreference(
            activity,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        val futureTimeout = timeout?.let { alarmProcess?.alarmTime?.timeInMillis?.plus(it * 1000) }

        if (futureTimeout != null) {
            val calendar = Calendar.getInstance()
            return when {
                futureTimeout < calendar.timeInMillis -> true
                else -> false
            }
        }
        return true
    }

    //remove alarm sensor if exist
    private fun removeSensorAlarmById(alarmId: String) {

        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (sensorItem.alarmSensorId == alarmId) {
                iteratorList.remove()
            }
        }
    }

    //check if the sensor is in alarm process
    private fun getSensorAlarmBySensor(sensor: Sensor): AlarmSensor? {

        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (sensorItem.alarmSensorId == sensor.getId()) {
                return sensorItem
            }
        }
        return null
    }

    var mySymbolCurrLocation: Symbol? = null

    //show marker of current location
    private fun showCurrentLocationMarker() {

        if (mapView == null) {
            return
        }

        if (myLocate == null) {
            return
        }


        if (myLocate != null) {
            currentLocationMarker = addMarker(
                myLocate!!,
                BLUE_ICON_ID,
                "myLocate",
                "",
                ""
            )
        }

    }

    //refresh markers
    private fun refreshMarkers() {

        if (markersList != null && markersList?.size!! > 0) {
            myMapboxMap?.setStyle(Style.Builder()
                .fromUri(mapType)

                // Add the SymbolLayer icon image to the map style
                .withImage(
                    GREEN_ICON_ID, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_sensor_item
                    )
                )
                .withImage(
                    BLUE_ICON_ID, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_my_locate
                    )
                )
                .withImage(
                    GRAY_ICON_ID, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_sensor_item_disable
                    )
                )
                .withImage(
                    ALARM_CAR_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_alarm_car
                    )
                )
                .withImage(
                    ALARM_FOOTSTEPS_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_alarm_intruder
                    )
                )
                .withImage(
                    ALARM_DIGGING_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_digging
                    )
                )
                .withImage(
                    ALARM_EXTERNAL_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_red_pin
                    )
                )
                .withImage(
                    ALARM_DISCONNCTED_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_alarm_sensor_off
                    )
                )
                .withImage(
                    ALARM_KEEP_ALIVE_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_green_pin
                    )
                )
                .withImage(
                    ALARM_LOW_BATTERY_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_alarm_low_battery
                    )
                )
                .withImage(
                    ALARM_DUAL_TECH_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_blue_pin
                    )
                )
                .withImage(
                    ALARM_GATEWAY_DISCONNECTED_STR, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_alarm_sensor_off
                    )
                )
                .withImage(
                    RED_ICON_ID, BitmapFactory.decodeResource(
                        requireActivity().resources, R.drawable.ic_sensor_alarm
                    )
                )

                // Adding a GeoJson source for the SymbolLayer icons.
                .withSource(
                    GeoJsonSource(
                        SOURCE_ID,
                        FeatureCollection.fromFeatures(markersList!!)
                    )
                )

                // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
                // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
                // the coordinate point. This is offset is not always needed and is dependent on the image
                // that you use for the SymbolLayer icon.
                .withLayer(
                    SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                            iconImage(
                                Expression.match(
                                    get(ICON_PROPERTY),
                                    Expression.literal(GREEN_ICON_ID),
                                    Expression.stop(GRAY_ICON_ID, GRAY_ICON_ID),
                                    Expression.stop(BLUE_ICON_ID, BLUE_ICON_ID),
                                    Expression.stop(GREEN_ICON_ID, GREEN_ICON_ID),
                                    Expression.stop(ALARM_CAR_STR, ALARM_CAR_STR),
                                    Expression.stop(ALARM_FOOTSTEPS_STR, ALARM_FOOTSTEPS_STR),
                                    Expression.stop(ALARM_DIGGING_STR, ALARM_DIGGING_STR),
                                    Expression.stop(ALARM_EXTERNAL_STR, ALARM_EXTERNAL_STR),
                                    Expression.stop(ALARM_DISCONNCTED_STR, ALARM_DISCONNCTED_STR),
                                    Expression.stop(ALARM_KEEP_ALIVE_STR, ALARM_KEEP_ALIVE_STR),
                                    Expression.stop(ALARM_LOW_BATTERY_STR, ALARM_LOW_BATTERY_STR),
                                    Expression.stop(ALARM_DUAL_TECH_STR, ALARM_DUAL_TECH_STR),
                                    Expression.stop(
                                        ALARM_GATEWAY_DISCONNECTED_STR,
                                        ALARM_GATEWAY_DISCONNECTED_STR
                                    ),
                                    Expression.stop(RED_ICON_ID, RED_ICON_ID)
                                )
                            ),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true)
                        )
                ), Style.OnStyleLoaded {
            })
        }//end checking the array
    }


    //add marker
    private fun addMarker(
        location: LatLng,
        iconId: String,
        cameraName: String?,
        type: String?,
        zone: String
    ): Feature? {

        val feature = Feature.fromGeometry(
            Point.fromLngLat(location.longitude, location.latitude)
        )


        val isSensorAlwaysShow = getBooleanInPreference(activity, IS_SENSOR_NAME_ALWAYS_KEY, false)
        if (!cameraName.equals("myLocate") && isSensorAlwaysShow) {
            feature.addStringProperty(PROPERTY_NAME, cameraName)
        }
        feature.addStringProperty(PROPERTY_NAME_WIN, cameraName)
        feature.addStringProperty(PROPERTY_SENSOR_TYPE, type)
        feature.addStringProperty(PROPERTY_ZONE, zone)
        feature.addStringProperty(ICON_PROPERTY, iconId)
        feature.addStringProperty(
            PROPERTY_COORDINATE,
            "${location.latitude} , ${location.longitude}"
        )

        markersList?.add(
            feature
        )

        if (markersList != null && markersList?.size!! > 0) {
            myMapboxMap?.setStyle(
                Style.Builder()
                    .fromUri(mapType)//"mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

                    // Add the SymbolLayer icon image to the map style
                    .withImage(
                        GREEN_ICON_ID, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_sensor_item
                        )
                    )
                    .withImage(
                        BLUE_ICON_ID, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_my_locate
                        )
                    )
                    .withImage(
                        GRAY_ICON_ID, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_sensor_item_disable
                        )
                    )
                    .withImage(
                        ALARM_CAR_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_alarm_car
                        )
                    )
                    .withImage(
                        ALARM_FOOTSTEPS_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_alarm_intruder
                        )
                    )
                    .withImage(
                        ALARM_DIGGING_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_digging
                        )
                    )
                    .withImage(
                        ALARM_EXTERNAL_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_red_pin
                        )
                    )
                    .withImage(
                        ALARM_DISCONNCTED_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_alarm_sensor_off
                        )
                    )
                    .withImage(
                        ALARM_KEEP_ALIVE_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_green_pin
                        )
                    )
                    .withImage(
                        ALARM_LOW_BATTERY_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_alarm_low_battery
                        )
                    )
                    .withImage(
                        ALARM_DUAL_TECH_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_blue_pin
                        )
                    )
                    .withImage(
                        ALARM_GATEWAY_DISCONNECTED_STR, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_alarm_sensor_off
                        )
                    )
                    .withImage(
                        RED_ICON_ID, BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_sensor_alarm
                        )
                    )

                    // Adding a GeoJson source for the SymbolLayer icons.
                    .withSource(
                        GeoJsonSource(
                            SOURCE_ID,
                            FeatureCollection.fromFeatures(markersList!!)
                        )
                    )

// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
// the coordinate point. This is offset is not always needed and is dependent on the image
// that you use for the SymbolLayer icon.
                    .withLayer(
                        SymbolLayer(LAYER_ID, SOURCE_ID)
                            .withProperties(
                                iconImage(
                                    Expression.match(
                                        get(ICON_PROPERTY),
                                        Expression.literal(GREEN_ICON_ID),
                                        Expression.stop(GRAY_ICON_ID, GRAY_ICON_ID),
                                        Expression.stop(BLUE_ICON_ID, BLUE_ICON_ID),
                                        Expression.stop(GREEN_ICON_ID, GREEN_ICON_ID),
                                        Expression.stop(ALARM_CAR_STR, ALARM_CAR_STR),
                                        Expression.stop(ALARM_FOOTSTEPS_STR, ALARM_FOOTSTEPS_STR),
                                        Expression.stop(ALARM_DIGGING_STR, ALARM_DIGGING_STR),
                                        Expression.stop(ALARM_EXTERNAL_STR, ALARM_EXTERNAL_STR),
                                        Expression.stop(
                                            ALARM_DISCONNCTED_STR,
                                            ALARM_DISCONNCTED_STR
                                        ),
                                        Expression.stop(ALARM_KEEP_ALIVE_STR, ALARM_KEEP_ALIVE_STR),
                                        Expression.stop(
                                            ALARM_LOW_BATTERY_STR,
                                            ALARM_LOW_BATTERY_STR
                                        ),
                                        Expression.stop(ALARM_DUAL_TECH_STR, ALARM_DUAL_TECH_STR),
                                        Expression.stop(
                                            ALARM_GATEWAY_DISCONNECTED_STR,
                                            ALARM_GATEWAY_DISCONNECTED_STR
                                        ),
                                        Expression.stop(RED_ICON_ID, RED_ICON_ID)
                                    )
                                ),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true),
                                textOffset(FloatArray(2) { 0f;-2.5f }.toTypedArray()),
                                textIgnorePlacement(true),
                                textAllowOverlap(true),
                                textHaloColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                ),
                                textHaloWidth(2f),
                                textVariableAnchor(Array(4) { TEXT_ANCHOR_TOP }),//; TEXT_ANCHOR_BOTTOM; TEXT_ANCHOR_LEFT; TEXT_ANCHOR_RIGHT}),
                                //textJustify(TEXT_JUSTIFY_AUTO),
                                textField(Expression.concat(get(PROPERTY_NAME)))
                            )
                    ), Style.OnStyleLoaded {
                })
        }//end checking the array
        return feature
    }


    //configureActivatio3n map type
    private fun initMapType() {
        val _mapType = getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)
        //Log.d("testMapView", "_mapType:$_mapType")
        if (_mapType == MAP_SHOW_NORMAL_VALUE) {
            mapType = Style.OUTDOORS
        } else if (_mapType == MAP_SHOW_SATELLITE_VALUE) {
            mapType = Style.SATELLITE
        }

    }

    private fun setMyLocate(myLocate: LatLng) {
        this.myLocate = myLocate
    }

    //get last location from shared preference
    private fun getLastLocationLocally(): LatLng? {
        val latitude = getStringInPreference(activity, CURRENT_LATITUDE_PREF, "-1")
        val longtude = getStringInPreference(activity, CURRENT_LONGTUDE_PREF, "-1")
        var lat: Double? = null
        var lon: Double? = null

        if (!latitude.equals("-1") && !longtude.equals("-1")) {
            try {
                lat = latitude?.toDouble()
                lon = longtude?.toDouble()

            } catch (ex: NumberFormatException) {
            }
        }
        if (lat != null && lon != null) {
            return LatLng(lat, lon)
        }
        return null
    }

    //remove all the time out sensors alarm and show them with regular sensor marker
    private fun replaceSensorAlarmTimeOutToSensorMarker() {
        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (isSensorAlarmTimeout(sensorItem)) {
                //show regular sensor marker
                sensorItem.markerFeature?.let {
                    showSensorMarker(
                        it,
                        sensorItem.isSensorArmed
                    )
                }
                //set sensor alarm sound as off because it timeout
                sensorItem.isSound = false
                //iteratorList.remove()
            }
        }
    }

    //show marker of sensor
    private fun showSensorMarker(
        markerFeature: Feature,
        isSensorArmed: Boolean

    ) {
        if (markerFeature == null || mapView == null) {
            return
        }


        if (isSensorArmed) {
            markerFeature.addStringProperty(ICON_PROPERTY, GREEN_ICON_ID)
        } else {
            markerFeature.addStringProperty(ICON_PROPERTY, GRAY_ICON_ID)
        }
        refreshMarkers()
    }

    private fun showDialogSensorsList() {

        //TODO to separate the adapters

        val sensors = activity?.let { getSensorsFromLocally(it) }

        if (dialog != null && dialog?.isShowing!!) {
            sensorsDialogAdapter?.setDetects(sensors)
            sensorsDialogAdapter?.notifyDataSetChanged()
            return
        }

        sensorsDialogAdapter = activity?.let { adapter ->
            sensors?.let { arr ->
                SensorsDialogAdapter(arr, adapter, this) { _ ->

                }
            }
        }

        //create dialog
        dialog = this.context?.let { Dialog(it) }
        //set layout custom
        dialog?.setContentView(R.layout.dialog_list_detectors)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        dialog?.window?.setLayout(width, height)

        val rvDetector = dialog?.findViewById<RecyclerView>(R.id.rvDetector)
        val btnSaveLocateSensor = dialog?.findViewById<Button>(R.id.btnSaveLocateSensor)
        btnSaveLocateSensor?.setOnClickListener {
            SensorsDialogAdapter.selectedSensor

            currentLatitude?.let { SensorsDialogAdapter.selectedSensor?.setLatitude(it) }
            currentLongitude?.let { SensorsDialogAdapter.selectedSensor?.setLongtitude(it) }

            SensorsDialogAdapter.selectedSensor?.let { sensor -> saveLatLongDetector(sensor) }
            dialog?.dismiss()
            showMarkers()

        }

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.divider
            )!!
        )
        rvDetector?.addItemDecoration(itemDecorator)

        sensorsDialogAdapter?.itemClick = { detector ->

        }

        // Add some item here to show the list.
        rvDetector?.adapter = sensorsDialogAdapter
        val mLayoutManager = LinearLayoutManager(context)
        rvDetector?.layoutManager = mLayoutManager
        dialog?.show()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapmobFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapmobFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setFilter() {
        val filter = IntentFilter(CREATE_ALARM_KEY)
        filter.addAction(RESET_MARKERS_KEY)
        filter.addAction(GET_CURRENT_LOCATION_KEY)
        filter.addAction(GET_CURRENT_SINGLE_LOCATION_KEY)
        filter.addAction(STOP_ALARM_SOUND)
        filter.addAction(ACTION_TOGGLE_TEST_MODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)
        } else {
            activity?.registerReceiver(usbReceiver, filter)
        }
    }

    override fun onStart() {
        super.onStart()
        setFilter()
        initMapType()
        mapView?.onStart()
    }


    override fun onResume() {
        super.onResume()

        mapView?.onResume()

        //load map
        if (isAdded) {
            mapView?.getMapAsync {
                mapView?.getMapAsync { mapboxMap ->
                    mapboxMap.uiSettings.isCompassEnabled = true
                    mapboxMap.uiSettings.setCompassFadeFacingNorth(false)
                    mapboxMap.setStyle(mapType) {

                        loadedMapStyle = it
                        loadedMapStyle?.addSource(GeoJsonSource("source-id"))
                        myMapboxMap = mapboxMap

                        myMapboxMap?.addOnMapClickListener { point ->

                            val result = handleClickIcon(
                                mapboxMap.projection.toScreenLocation(point),
                                point
                            )
                            result
                        }

                        //detect map dragging
                        mapboxMap.addOnMoveListener(this)

                        //Bug fixed: no need to open dialog to add sensor
//                        myMapboxMap?.addOnMapLongClickListener { point ->
//                            currentLongitude = point.longitude
//                            currentLatitude = point.latitude
//                            showDialogSensorsList()
//                            true
//                        }

                        //for markers
                        markerViewManager = MarkerViewManager(mapView, myMapboxMap)


                        //go to last location
                        val location = initFindLocation()


                        //set last location if exist
                        location?.let {
                            myLocate =
                                LatLng(it.latitude, it.longitude)
                        }

                        showLocation(location)

                        gotoMySingleLocation()
                    }
                }
            }

        }
    }


    // move the camera to ic_mark location
    private fun showMyLocationMarker(location: Location?) {

        if (location != null) {
            setMyLocate(
                LatLng(
                    location.latitude,
                    location.longitude
                )
            )
        } else {

            myLocate = getLastLocationLocally()

            if (myLocate == null) {
                //set default location (london)
                myLocate = LatLng(51.509865, -0.118092)
                //set default location (london) if there is no last location
                setMyLocate(LatLng(51.509865, -0.118092))
            }
        }
        //add marker at the focus of the map
        myLocate?.let {
            //show current location marker
            showCurrentLocationMarker()

        }

    }

    private fun saveLatLongDetector(sensor: Sensor) {
        val sensorsArr = activity?.let { getSensorsFromLocally(it) }
        if (sensorsArr != null) {

            val iteratorList = sensorsArr.listIterator()
            while (iteratorList != null && iteratorList.hasNext()) {
                val sensorItem = iteratorList.next()
                if (sensorItem.getId() == sensor.getId()) {
                    sensor.getLatitude()?.let { sensorItem.setLatitude(it) }
                    sensor.getLongtitude()?.let { sensorItem.setLongtitude(it) }
                }
            }

        }
        sensorsArr?.let { activity?.let { context -> storeSensorsToLocally(it, context) } }
    }


    override fun onPause() {
        super.onPause()
        popup?.dismiss()
        mapView?.onPause()
        activity?.stopService(Intent(context, ServiceFindLocation::class.java))
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        activity?.unregisterReceiver(usbReceiver)
    }

    //save the name of the sensor
    override fun saveNameSensor(detector: Sensor) {
        val detectorsArr = activity?.let { getSensorsFromLocally(it) }
        if (detectorsArr != null) {

            val iteratorList = detectorsArr.listIterator()
            while (iteratorList != null && iteratorList.hasNext()) {
                val detectorItem = iteratorList.next()
                if (detectorItem.getId() == detector.getId()) {
                    detector.getName()?.let { detectorItem.setName(it) }
                }
            }

        }
        detectorsArr?.let { activity?.let { context -> storeSensorsToLocally(it, context) } }
        showDialogSensorsList()
    }

    override fun saveSensors(detector: Sensor) {}

    //reciever
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == CREATE_ALARM_KEY) {

                val alarmSensorId = inn.getStringExtra(CREATE_ALARM_ID_KEY)
                val type = inn.getStringExtra(CREATE_ALARM_TYPE_KEY)
                val typeIdx = inn.getIntExtra(CREATE_ALARM_TYPE_INDEX_KEY, -1)
                val isArmed = inn.getBooleanExtra(CREATE_ALARM_IS_ARMED, false)
                Log.d("testAlarmMap", "MapmobFragment:showMarkers")

//                //prevent duplicate alarm at the same sensor at the same time
//                alarmSensorId?.let { removeSensorAlarmById(it) }
//
//                //add alarm process to queue
//                val alarmSensor = alarmSensorId.let {
//                    it?.let { it1 ->
//                        type?.let { it2 ->
//                            AlarmSensor(
//                                it1,
//                                Calendar.getInstance(),
//                                it2,
//                                isArmed
//                            )
//                        }
//                    }
//                }
//                alarmSensor?.typeIdx = typeIdx
//                alarmSensor?.let { UserSession.instance.alarmSensors?.add(it) }
                showMarkers()

            }
//            else if (inn.action == GET_CURRENT_LOCATION_KEY) {
//                val location: Location? = inn.getParcelableExtra(CURRENT_LOCATION)
//                if (location != null) {
//                    //save locally the current location
//                    setStringInPreference(
//                        activity,
//                        CURRENT_LATITUDE_PREF,
//                        location.latitude.toString()
//                    )
//                    setStringInPreference(
//                        activity,
//                        CURRENT_LONGTUDE_PREF,
//                        location.longitude.toString()
//                    )
//                    //clear the current marker
//                    markersList?.remove(currentLocationMarker)
//                    showMyLocationMarker(location)
//                    //move the camera
//                    //moveCamera()
//                    //showLocation(location)
//                } else {
//                    Toast.makeText(activity, "error in location2", Toast.LENGTH_LONG).show()
//                }
//            }
            else if (inn.action == GET_CURRENT_SINGLE_LOCATION_KEY) {
                val location: Location? = inn.getParcelableExtra(CURRENT_LOCATION)
                if (location != null) {
                    //save locally the current location
                    setStringInPreference(
                        activity,
                        CURRENT_LATITUDE_PREF,
                        location.latitude.toString()
                    )
                    setStringInPreference(
                        activity,
                        CURRENT_LONGTUDE_PREF,
                        location.longitude.toString()
                    )
                    showLocation(location)
                } else {
                    Toast.makeText(activity, "error in location2", Toast.LENGTH_LONG).show()
                }
            } else if (inn.action == RESET_MARKERS_KEY) {
                showMarkers()
            } else if (inn.action == STOP_ALARM_SOUND) {
                //stopPlayingAlarm()
            }
            ////Bugs fixed: disable alarm test
//            else if (inn.action == ACTION_TOGGLE_TEST_MODE) {
//                if (fbTest?.visibility == View.VISIBLE) {
//                    fbTest?.visibility = View.GONE
//                } else {
//                    fbTest?.visibility = View.VISIBLE
//                }
//            }

        }
    }

    /**
     * move the camera
     */
    private fun moveCamera() {
        myLocate?.let {
            //load the camera
            if (myLocate != null && myLocate?.latitude != null &&
                myLocate?.longitude != null
            ) {

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(myLocate?.latitude!!, myLocate?.longitude!!))
                    .zoom(ZOMM_LEVEL)
                    .tilt(20.0)
                    .build()


                // Move camera to new position
                myMapboxMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


            }
        }
    }

    private val PROPERTY_NAME = "name"
    private val PROPERTY_NAME_WIN = "name_win"
    private val PROPERTY_SENSOR_TYPE = "sensor_type"
    private val PROPERTY_ZONE = "sensor_zone"
    private val PROPERTY_COORDINATE = "sensor_coordinates"

    private fun handleClickIcon(screenPoint: PointF, point: LatLng): Boolean {
        if (myMapboxMap != null) {
            val features: List<Feature> = myMapboxMap!!.queryRenderedFeatures(screenPoint, LAYER_ID)
            if (features.isNotEmpty()) {
                val cameraName = features[0].getStringProperty(PROPERTY_NAME_WIN)
                val sensorType = features[0].getStringProperty(PROPERTY_SENSOR_TYPE)
                val unit = features[0].getStringProperty(PROPERTY_ZONE)
                val coordinates = features[0].getStringProperty(PROPERTY_COORDINATE)
                //if(cameraName!=null && cameraName != "") {
                showPopup(requireActivity(), screenPoint, cameraName, sensorType, unit, coordinates)
                //}

                return true
            } else {
                if (popup != null)
                    popup?.dismiss()
                return false
            }
        }
        return false
    }


    //popup with camera info
    private fun showPopup(
        context: Activity,
        pointF: PointF,
        cameraName: String,
        sensorType: String,
        unit: String,
        coordinates: String
    ) {

        //when press on icon of current location
        if (cameraName == "myLocate")
            return

        // Inflate the popup_layout.xml
        //val viewGroup = context.findViewById<View>(R.id.popup) as LinearLayout
        val layoutInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.popup_marker, null)

        // Creating the PopupWindow
        if (popup != null)
            popup?.dismiss()

        popup = PopupWindow(context)
        popup?.contentView = layout

        popup?.isFocusable = false


        //disregard the tab layout height
        val offsetY = dpToPx(TABLAYOUT_HEIGHT_DEFAULT, requireActivity())


        // Displaying the popup at the specified location, + offsets.
        popup?.showAtLocation(
            layout,
            Gravity.NO_GRAVITY,
            pointF.x.toInt(),
            pointF.y.toInt() + offsetY
        )

        // Getting a reference to Close button, and close the popup when clicked.
        val tvCameraName = layout.findViewById<TextView>(R.id.tvCameraName)
        tvCameraName.text = cameraName

        val tvCameraType = layout.findViewById<TextView>(R.id.tvCameraType)
        tvCameraType.text = sensorType//cameraName

        val tvUnit = layout.findViewById<TextView>(R.id.tvUnit)
        tvUnit.text = unit

        val tvCoordinates = layout.findViewById<TextView>(R.id.tvCoordinates)
        tvCoordinates.text = coordinates
    }

    //to dismiss info popup when
    override fun onMoveBegin(detector: MoveGestureDetector) {
        if (popup != null)
            popup?.dismiss()
    }

    override fun onMove(detector: MoveGestureDetector) {}

    override fun onMoveEnd(detector: MoveGestureDetector) {}


}


