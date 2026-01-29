package com.sensoguard.ccsmobileclient.fragments

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.OfflineRegion
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.adapters.SensorsDialogAdapter
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.classes.Sensor
import com.sensoguard.ccsmobileclient.controler.ViewModelListener
import com.sensoguard.ccsmobileclient.global.ACTION_TOGGLE_TEST_MODE
import com.sensoguard.ccsmobileclient.global.ALARM_CAR
import com.sensoguard.ccsmobileclient.global.ALARM_CAR_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DIGGING_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNCTED
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNCTED_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH_STR
import com.sensoguard.ccsmobileclient.global.ALARM_EXTERNAL_STR
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.ALARM_FOOTSTEPS_STR
import com.sensoguard.ccsmobileclient.global.ALARM_GATEWAY_DISCONNECTED
import com.sensoguard.ccsmobileclient.global.ALARM_GATEWAY_DISCONNECTED_STR
import com.sensoguard.ccsmobileclient.global.ALARM_INTRUDER
import com.sensoguard.ccsmobileclient.global.ALARM_KEEP_ALIVE
import com.sensoguard.ccsmobileclient.global.ALARM_KEEP_ALIVE_STR
import com.sensoguard.ccsmobileclient.global.ALARM_LOW_BATTERY
import com.sensoguard.ccsmobileclient.global.ALARM_LOW_BATTERY_STR
import com.sensoguard.ccsmobileclient.global.ALARM_MOTION
import com.sensoguard.ccsmobileclient.global.ALARM_SENSOR_OFF
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_ID_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_IS_ARMED
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_INDEX_KEY
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_LATITUDE_PREF
import com.sensoguard.ccsmobileclient.global.CURRENT_LOCATION
import com.sensoguard.ccsmobileclient.global.CURRENT_LONGTUDE_PREF
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_LOCATION_KEY
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_SINGLE_LOCATION_KEY
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.RESET_MARKERS_KEY
import com.sensoguard.ccsmobileclient.global.STOP_ALARM_SOUND
import com.sensoguard.ccsmobileclient.global.TABLAYOUT_HEIGHT_DEFAULT
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.dpToPx
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.interfaces.OnAdapterListener
import com.sensoguard.ccsmobileclient.services.ServiceFindLocation
import com.sensoguard.ccsmobileclient.services.ServiceFindSingleLocation
import java.util.*

class MapmobFragment1 : ParentFragment(), OnAdapterListener, OnMoveListener {

    private var popup: PopupWindow? = null
    private var currentLocationMarker: Feature? = null
    private var markersList: ArrayList<Feature>? = null
    //hag private var symbolOption: SymbolOptions? = null
    //hag private var markerViewManager: MarkerViewManager? = null

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
    private val LOW_BATTERY_ICON_ID = "LOW_BATTERY_ID"
    private val ZOMM_LEVEL = 5.0

    private var locationManager: LocationManager? = null
    private var isPaused = false

    //annotations (markers)
    private var viewAnnotationManager: ViewAnnotationManager? = null//mapView?.viewAnnotationManager
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var annotationApi: AnnotationPlugin? = null
    private var pointAnnotation: PointAnnotation? = null
    //////////////

    private var currentPopup: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTimerListener()
    }

    /**
     * start listener to timer
     */
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
                            //check if needed hag
                            //set all the time out sensors alarm as sound off
                            //replaceSensorAlarmTimeOutToSensorMarker()
                            //showMarkers()
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

    /**
    //remove all the time out sensors alarm and show them with regular sensor marker
     *
     */
//    private fun replaceSensorAlarmTimeOutToSensorMarker() {
//        val iteratorList = UserSession.instance.alarmSensors?.listIterator()
//        while (iteratorList != null && iteratorList.hasNext()) {
//            val sensorItem = iteratorList.next()
//            if (isSensorAlarmTimeout(sensorItem)) {
//                //show regular sensor marker
//                sensorItem.markerFeature?.let {
//                    showSensorMarker(
//                        it,
//                        sensorItem.isSensorArmed
//                    )
//                }
//                //set sensor alarm sound as off because it timeout
//                sensorItem.isSound = false
//                //iteratorList.remove()
//            }
//        }
//    }

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_mapmob, container, false)

        mapView = view.findViewById(R.id.mapView)
        //mapView?.onCreate(savedInstanceState)


        fbRefresh = view.findViewById(R.id.fbRefresh1)
        fbRefresh?.setOnClickListener {
            gotoMySingleLocation()
        }

        fbClear = view.findViewById(R.id.fbClear)
        fbClear?.setOnClickListener {
            //hag clearAlarms()
        }

        fbTest = view.findViewById(R.id.fbTest1)
        fbTest?.setOnClickListener {
            //hag showTestEventDialog()
        }


        initMapType()


        return view
    }

    override fun onStart() {
        super.onStart()
        setFilter()
        initMapType()
        //mapView?.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        //mapView?.onDestroy()
        activity?.unregisterReceiver(usbReceiver)
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

    /**
     * configureActivation map type
     */
    private fun initMapType() {
        val _mapType = getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)
        //Log.d("testMapView", "_mapType:$_mapType")
        if (_mapType == MAP_SHOW_NORMAL_VALUE) {
            mapType = Style.OUTDOORS
        } else if (_mapType == MAP_SHOW_SATELLITE_VALUE) {
            mapType = Style.SATELLITE
        }
    }

    override fun onResume() {
        super.onResume()

        isPaused = false
        //load map
        if (isAdded) {

            myMapboxMap = mapView?.mapboxMap

            myMapboxMap?.loadStyle(mapType)

            //detect map dragging
            myMapboxMap?.addOnMoveListener(this)

            myMapboxMap?.addOnMapLongClickListener { point ->
                currentLongitude = point.longitude()
                currentLatitude = point.latitude()
                //showDialogSensorsList()
                true
            }

            //go to last location
            val location = initFindLocation()


            //set last location if exist
            location?.let {
                myLocate =
                    LatLng(it.latitude, it.longitude)
            }

            showLocation(location)

            gotoMyLocation()

        }
    }

    /**
     * get current location from gps
     */
    private fun gotoMyLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(context, ServiceFindLocation::class.java))
        } else {
            activity?.startService(Intent(context, ServiceFindLocation::class.java))
        }
    }

    private fun setMyLocate(myLocate: LatLng) {
        this.myLocate = myLocate
    }

    /**
     * Done move the camera to ic_mark location
     */
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

                pointAnnotationManager =
                    mapView?.annotations?.createPointAnnotationManager().apply {

                        val cameraPosition = CameraOptions.Builder()
                            .zoom(ZOMM_LEVEL)
                            .center(
                                Point.fromLngLat(
                                    myLocate?.longitude!!,
                                    myLocate?.latitude!!
                                )
                            )//Point.fromLngLat(myLocate?.latitude!!, myLocate?.longitude!!))
                            .build()
                        // set camera position
                        myMapboxMap?.setCamera(cameraPosition)
                    }
                //show all markers
                showMarkers()

            }

        }

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

    /**
     * get last location
     */
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

                showMarkers()

            } else if (inn.action == GET_CURRENT_SINGLE_LOCATION_KEY) {
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

    //get current location from gps
    private fun gotoMySingleLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(context, ServiceFindSingleLocation::class.java))
        } else {
            activity?.startService(Intent(context, ServiceFindSingleLocation::class.java))
        }
    }




    /**
     * show all markers
     */
    fun showMarkers() {

//remove all markers
        pointAnnotationManager?.deleteAll()
        pointAnnotation = null

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
                    //hag check if it needed
                    //show sensor marker
                    //showSensorMarker(sensorItem)
                }
            }
        }

    }


    /**
     * show marker of current location if exist
     */
    private fun showCurrentLocationMarker() {

        if (activity == null) {
            return
        }

        if (mapView == null) {
            return
        }

        if (myLocate == null) {
            return
        }

        if (myLocate != null) {

            if (pointAnnotation == null) {
                // Set options for the resulting symbol layer.
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    // Define a geographic coordinate.
                    .withPoint(Point.fromLngLat(myLocate?.longitude!!, myLocate?.latitude!!))
                    // Specify the bitmap you assigned to the point annotation
                    // The bitmap will be added to map style automatically.
                    .withIconImage(
                        BitmapFactory.decodeResource(
                            requireActivity().resources, R.drawable.ic_my_locate
                        )
                    )
                // Add the resulting pointAnnotation to the map.
                pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
            } else {
                //if pointAnnotation is already exist then update the current markers location
                pointAnnotation?.point =
                    Point.fromLngLat(myLocate?.longitude!!, myLocate?.latitude!!)
                if (pointAnnotation != null) {
                    pointAnnotationManager?.update(pointAnnotation!!)
                }
            }
        }
    }


    /**
     * sort the alarm by id
     */
    private fun sortByIdAlarm(sensors: ArrayList<Sensor>?): List<Sensor>? {
        return sensors?.sortedBy { it.getId().toInt() }

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

                ALARM_INTRUDER -> {
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

                ALARM_MOTION -> {
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

                ALARM_SENSOR_OFF -> {
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
     * add one marker to the map
     */
    private fun addMarker(
        location: LatLng,
        iconId: String,
        cameraName: String?,
        type: String?,
        zone: String
    ): Feature? {

        var myIcon: Int? = null

        when (iconId) {
            GREEN_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_item
            }

            BLUE_ICON_ID -> {
                myIcon = R.drawable.ic_my_locate
            }

            GRAY_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_item_disable
            }

            ALARM_CAR_STR -> {
                myIcon = R.drawable.ic_alarm_car
            }

            ALARM_FOOTSTEPS_STR -> {
                myIcon = R.drawable.ic_alarm_intruder
            }

            ALARM_DIGGING_STR -> {
                myIcon = R.drawable.ic_digging
            }

            ALARM_EXTERNAL_STR -> {
                myIcon = R.drawable.ic_red_pin
            }

            ALARM_DISCONNCTED_STR -> {
                myIcon = R.drawable.ic_alarm_sensor_off
            }

            ALARM_KEEP_ALIVE_STR -> {
                myIcon = R.drawable.ic_green_pin
            }

            ALARM_LOW_BATTERY_STR -> {
                myIcon = R.drawable.ic_alarm_low_battery
            }

            ALARM_DUAL_TECH_STR -> {
                myIcon = R.drawable.ic_blue_pin
            }

            ALARM_GATEWAY_DISCONNECTED_STR -> {
                myIcon = R.drawable.ic_alarm_sensor_off
            }

            RED_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_alarm
            }

            else -> {}
        }

        if (myIcon == null) {
            return null
        }

        // Set options for the resulting symbol layer.
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(Point.fromLngLat(location.longitude, location.latitude))
            // Specify the bitmap you assigned to the point annotation
            // The bitmap will be added to map style automatically.
            .withIconImage(
                BitmapFactory.decodeResource(
                    requireActivity().resources, myIcon
                )
            )
        val coordinates = "${location.latitude},${location.longitude}"
        pointAnnotationOptions.textField = "$cameraName:$type:$zone:$coordinates"
        //pointAnnotationOptions.textField = "$cameraName:$type"

        //set transparent to hide preview text (without click)
        //TODO to learn how to hide annotation view text
        pointAnnotationOptions.withTextColor(Color.TRANSPARENT)
        // Add the resulting pointAnnotation to the map.
        pointAnnotationManager?.create(pointAnnotationOptions)
        pointAnnotationManager?.addClickListener(object : OnPointAnnotationClickListener {
            override fun onAnnotationClick(annotation: PointAnnotation): Boolean {


                // Remove existing popup if any
                viewAnnotationManager?.removeAllViewAnnotations()

                currentPopup = createPopup(annotation)


                val options: ViewAnnotationOptions?
                options = viewAnnotationOptions {
                    geometry(
                        Point.fromLngLat(
                            annotation.point.longitude(),
                            annotation.point.latitude()
                        )
                    )
                    allowOverlap(false)
                    annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                    //visible(false)
                }

                val lp = LinearLayout.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT
                )
                currentPopup?.layoutParams = lp


                if (currentPopup != null) {
                    // Add the popup as a ViewAnnotation
                    viewAnnotationManager?.addViewAnnotation(currentPopup!!, options)
                }


                return true
            }
        })
        return null
    }


    /**
     * create popup for sensor (with marker) info
     */
    private fun createPopup(annotation: PointAnnotation): View {
        // Inflate the popup layout
        val layoutInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popup: View = layoutInflater.inflate(R.layout.popup_marker, null)


        val arr = annotation.textField.toString().split(":")
        popup.findViewById<TextView>(R.id.tvCameraName).text = arr[0]
        popup.findViewById<TextView>(R.id.tvCameraType).text = arr[1]
        popup.findViewById<TextView>(R.id.tvUnit).text = arr[2]
        popup.findViewById<TextView>(R.id.tvCoordinates).text = arr[3]

        return popup
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


    /**
     * move the camera to location of alarm
     */
    private fun moveCamera(loc: LatLng?) {
        if (loc != null) {
            val cameraPosition = CameraOptions.Builder()
                .zoom(ZOMM_LEVEL)
                .center(
                    Point.fromLngLat(
                        loc.longitude,
                        loc.latitude
                    )
                )//Point.fromLngLat(myLocate?.latitude!!, myLocate?.longitude!!))
                .build()
            // set camera position
            myMapboxMap?.setCamera(cameraPosition)
        }
    }



    override fun saveNameSensor(detector: Sensor) {}

    override fun saveSensors(detector: Sensor) {}

    override fun onMove(detector: MoveGestureDetector): Boolean {
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {}

    override fun onMoveEnd(detector: MoveGestureDetector) {}

}