package com.sensoguard.ccsmobileclient.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.AlarmSensor
import com.sensoguard.ccsmobileclient.controler.ViewModelListener
import com.sensoguard.ccsmobileclient.global.ALARM_CAR
import com.sensoguard.ccsmobileclient.global.ALARM_CAR_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DIGGING_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNECTED
import com.sensoguard.ccsmobileclient.global.ALARM_DISCONNECTED_STR
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH
import com.sensoguard.ccsmobileclient.global.ALARM_DUAL_TECH_STR
import com.sensoguard.ccsmobileclient.global.ALARM_EXTERNAL_STR
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
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_LATITUDE_PREF
import com.sensoguard.ccsmobileclient.global.CURRENT_LOCATION
import com.sensoguard.ccsmobileclient.global.CURRENT_LONGITUDE_PREF
import com.sensoguard.ccsmobileclient.global.GET_CURRENT_SINGLE_LOCATION_KEY
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.UserSession
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.services.ServiceFindSingleLocation

class MapmobFragment : ParentFragment() {

    private var mapView: MapView? = null
    private var mapType = Style.OUTDOORS
    private var myLocate: Point? = null

    private var fbRefresh: FloatingActionButton? = null
    private var fbClear: FloatingActionButton? = null

    private var myMapboxMap: MapboxMap? = null

    private var isPaused = false

    //annotations (markers)
    private var viewAnnotationManager: ViewAnnotationManager? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var pointAnnotation: PointAnnotation? = null

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
            ViewModelProvider(it)[ViewModelListener::class.java]
                .startCurrentCalendarListener().observe(this) {
                    Log.d(TAG, "startTimerListener")
                    //if there is no alarm in process then shut down the timer
                    if (UserSession.instance.alarmSensors.isEmpty()
                        || isAllSensorAlarmTimeOutSound()
                    ) {
                        activity?.let { act ->
                            ViewModelProvider(act)[ViewModelListener::class.java].shutDownTimer()
                        }
                    }
                    showMarkers()
                }
        }
    }

    /**
     * check if all the alarms are timeout for sound only
     */
    private fun isAllSensorAlarmTimeOutSound(): Boolean {
        return UserSession.instance.alarmSensors.none { it.isSound }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mapmob, container, false)

        mapView = view.findViewById(R.id.mapView)

        mapView?.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // Remove existing popup if any when press outside
                    viewAnnotationManager?.removeAllViewAnnotations()
                }

                MotionEvent.ACTION_UP -> v.performClick()
            }
            false
        }

        fbRefresh = view.findViewById(R.id.fbRefresh1)
        fbRefresh?.setOnClickListener {
            gotoMySingleLocation()
        }

        fbClear = view.findViewById(R.id.fbClear)
        fbClear?.setOnClickListener {
            clearAlarms()
        }

        initMapType()

        myMapboxMap = mapView?.mapboxMap

        initializeAnnotation()

        return view
    }

    override fun onStart() {
        super.onStart()
        setFilter()
        initMapType()
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(usbReceiver)
    }

    override fun onPause() {
        super.onPause()
        currentPopup?.let { viewAnnotationManager?.removeViewAnnotation(it) }
        pointAnnotationManager?.deleteAll()
        pointAnnotation = null
        isPaused = true
    }

    private fun setFilter() {
        val filter = IntentFilter(CREATE_ALARM_KEY)
        filter.addAction(GET_CURRENT_SINGLE_LOCATION_KEY)
        ContextCompat.registerReceiver(
            requireActivity(),
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    /**
     * configureActivation map type
     */
    private fun initMapType() {
        when (getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)) {
            MAP_SHOW_NORMAL_VALUE -> mapType = Style.OUTDOORS
            MAP_SHOW_SATELLITE_VALUE -> mapType = Style.SATELLITE
        }
    }

    override fun onResume() {
        super.onResume()

        isPaused = false
        //load map
        if (isAdded) {
            myMapboxMap?.loadStyle(mapType)

            //go to last location
            showLocation(initFindLocation())

            gotoMySingleLocation()
        }
    }

    /**
     * initialize annotation for markers
     */
    private fun initializeAnnotation() {
        viewAnnotationManager = mapView?.viewAnnotationManager
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        pointAnnotationManager = mapView?.annotations?.createPointAnnotationManager()
        // show popup with the sensor info when press on marker
        pointAnnotationManager?.addClickListener { annotation ->
            showPopup(annotation)
            true
        }
    }

    /**
     * Done move the camera to ic_mark location
     */
    private fun showLocation(location: Location?) {

        myLocate = if (location != null) {
            Point.fromLngLat(location.longitude, location.latitude)
        } else {
            //set default location (london) if there is no last location
            getLastLocationLocally() ?: Point.fromLngLat(-0.118092, 51.509865)
        }

        myLocate?.let { moveCamera(it) }
        //show all markers
        showMarkers()
    }

    //get last location from shared preference
    private fun getLastLocationLocally(): Point? {
        val lat = getStringInPreference(activity, CURRENT_LATITUDE_PREF, "-1")?.toDoubleOrNull()
        val lon = getStringInPreference(activity, CURRENT_LONGITUDE_PREF, "-1")?.toDoubleOrNull()

        if (lat != null && lon != null && lat != -1.0 && lon != -1.0) {
            return Point.fromLngLat(lon, lat)
        }
        return null
    }

    /**
     * get last location
     */
    private fun initFindLocation(): Location? {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val context = context ?: return null
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        return null
    }

    //receiver
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            when (inn.action) {
                //accept currentAlarm
                CREATE_ALARM_KEY -> {
                    if (!isPaused) {
                        showMarkers()
                    }
                }

                GET_CURRENT_SINGLE_LOCATION_KEY -> {
                    val location =
                        IntentCompat.getParcelableExtra(inn, CURRENT_LOCATION, Location::class.java)
                    if (location != null) {
                        //save locally the current location
                        setStringInPreference(
                            activity,
                            CURRENT_LATITUDE_PREF,
                            location.latitude.toString()
                        )
                        setStringInPreference(
                            activity,
                            CURRENT_LONGITUDE_PREF,
                            location.longitude.toString()
                        )
                        showLocation(location)
                    } else {
                        Toast.makeText(activity, R.string.error_in_location, Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }
        }
    }

    //get current location from gps
    private fun gotoMySingleLocation() {
        val activity = activity ?: return
        if (ServiceFindSingleLocation.hasLocationPermission(activity)) {
            activity.startForegroundService(Intent(activity, ServiceFindSingleLocation::class.java))
        }
    }


    /**
     * show all markers
     */
    fun showMarkers() {

        //remove all markers
        pointAnnotationManager?.deleteAll()
        pointAnnotation = null

        //show current location marker
        showCurrentLocationMarker()

        UserSession.instance.alarmSensors.forEach { sensorItem ->
            if (sensorItem.latitude != null && sensorItem.longitude != null) {
                showSensorAlarmMarker(sensorItem)
            }
        }
    }


    /**
     * show marker of current location if exist
     */
    private fun showCurrentLocationMarker() {
        val activity = activity ?: return
        val myLocate = myLocate ?: return
        if (mapView == null) {
            return
        }

        val point = myLocate
        val currentPointAnnotation = pointAnnotation
        if (currentPointAnnotation == null) {
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(point)
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(
                    BitmapFactory.decodeResource(activity.resources, R.drawable.ic_my_locate)
                )
            // Add the resulting pointAnnotation to the map.
            pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
        } else {
            //if pointAnnotation is already exist then update the current markers location
            currentPointAnnotation.point = point
            pointAnnotationManager?.update(currentPointAnnotation)
        }
    }

    /***
     * show marker of sensor alarm
     */
    private fun showSensorAlarmMarker(sensorItem: AlarmSensor) {

        if (mapView == null) {
            return
        }

        val loc = Point.fromLngLat(sensorItem.longitude!!, sensorItem.latitude!!)

        val iconId = when (sensorItem.typeIdx) {
            ALARM_CAR -> ALARM_CAR_STR
            ALARM_INTRUDER -> ALARM_FOOTSTEPS_STR
            ALARM_MOTION -> ALARM_DIGGING_STR
            ALARM_SENSOR_OFF -> ALARM_EXTERNAL_STR
            ALARM_DISCONNECTED -> ALARM_DISCONNECTED_STR
            ALARM_KEEP_ALIVE -> ALARM_KEEP_ALIVE_STR
            ALARM_LOW_BATTERY -> ALARM_LOW_BATTERY_STR
            ALARM_DUAL_TECH -> ALARM_DUAL_TECH_STR
            ALARM_GATEWAY_DISCONNECTED -> ALARM_GATEWAY_DISCONNECTED_STR
            else -> RED_ICON_ID
        }
        addMarker(loc, iconId, sensorItem.alarmSensorId, sensorItem.type, sensorItem.zone)

        moveCamera(loc)
    }

    /**
     * add one marker to the map
     */
    private fun addMarker(
        location: Point,
        iconId: String,
        cameraName: String?,
        type: String?,
        zone: String
    ) {
        val myIcon = when (iconId) {
            ALARM_CAR_STR -> R.drawable.ic_alarm_car
            ALARM_FOOTSTEPS_STR -> R.drawable.ic_alarm_intruder
            ALARM_DIGGING_STR -> R.drawable.ic_digging
            ALARM_EXTERNAL_STR -> R.drawable.ic_red_pin
            ALARM_DISCONNECTED_STR -> R.drawable.ic_alarm_sensor_off
            ALARM_KEEP_ALIVE_STR -> R.drawable.ic_green_pin
            ALARM_LOW_BATTERY_STR -> R.drawable.ic_alarm_low_battery
            ALARM_DUAL_TECH_STR -> R.drawable.ic_blue_pin
            ALARM_GATEWAY_DISCONNECTED_STR -> R.drawable.ic_alarm_sensor_off
            else -> R.drawable.ic_sensor_alarm
        }

        // Set options for the resulting symbol layer.
        val pointAnnotationOptions = PointAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(location)
            // Specify the bitmap you assigned to the point annotation
            // The bitmap will be added to map style automatically.
            .withIconImage(BitmapFactory.decodeResource(requireActivity().resources, myIcon))
        val coordinates = "${location.latitude()},${location.longitude()}"
        //the popup info is kept in the text field of the marker
        pointAnnotationOptions.textField = "$cameraName:$type:$zone:$coordinates"

        //set transparent to hide preview text (without click)
        pointAnnotationOptions.withTextColor(Color.TRANSPARENT)
        // Add the resulting pointAnnotation to the map.
        pointAnnotationManager?.create(pointAnnotationOptions)
    }

    /**
     * show popup with the sensor info above the marker
     */
    private fun showPopup(annotation: PointAnnotation) {
        // Remove existing popup if any
        viewAnnotationManager?.removeAllViewAnnotations()

        //the marker of current location has no info
        val popup = createPopup(annotation) ?: return
        currentPopup = popup

        val options = viewAnnotationOptions {
            geometry(annotation.point)
            allowOverlap(false)
            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
        }

        popup.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

        // Add the popup as a ViewAnnotation
        viewAnnotationManager?.addViewAnnotation(popup, options)
    }

    /**
     * create popup for sensor (with marker) info
     */
    private fun createPopup(annotation: PointAnnotation): View? {
        val arr = annotation.textField?.split(":") ?: return null
        if (arr.size <= 3) {
            return null
        }

        // Inflate the popup layout
        val mapView = mapView ?: return null
        val popup: View = layoutInflater.inflate(R.layout.popup_marker, mapView, false)

        popup.findViewById<TextView>(R.id.tvCameraName).text = arr[0]
        popup.findViewById<TextView>(R.id.tvCameraType).text = arr[1]
        popup.findViewById<TextView>(R.id.tvUnit).text = arr[2]
        popup.findViewById<TextView>(R.id.tvCoordinates).text = arr[3]

        return popup
    }


    /**
     * move the camera to location
     */
    private fun moveCamera(loc: Point) {
        val cameraPosition = CameraOptions.Builder()
            .zoom(ZOOM_LEVEL)
            .center(loc)
            .build()
        // set camera position
        myMapboxMap?.setCamera(cameraPosition)
    }


    /**
     * clear all the alarms
     */
    private fun clearAlarms() {
        UserSession.instance.alarmSensors.clear()
        showMarkers()
    }

    companion object {
        private const val TAG = "MapmobFragment"
        private const val RED_ICON_ID = "RED_ICON_ID"
        private const val ZOOM_LEVEL = 5.0
    }
}
