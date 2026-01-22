package com.sensoguard.ccsmobileclient.fragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.adapters.SensorsDialogAdapter
import com.sensoguard.ccsmobileclient.classes.Sensor
import com.sensoguard.ccsmobileclient.global.ACTION_TOGGLE_TEST_MODE
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
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getSensorsFromLocally
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.global.storeSensorsToLocally
import com.sensoguard.ccsmobileclient.interfaces.OnAdapterListener
import com.sensoguard.ccsmobileclient.services.ServiceFindLocation
import com.sensoguard.ccsmobileclient.services.ServiceFindSingleLocation

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //startTimerListener()
    }

    //start listener to timer
//    private fun startTimerListener() {
//        activity?.let {
//            ViewModelProviders.of(it).get(ViewModelListener::class.java)
//                .startCurrentCalendarListener()?.observe(
//                    this,
//                    { calendar ->
//                        Log.d("testAlarmMap", "startTimerListener in MapSensorsFragment")
//                        //Log.d("testTimer","tick in MapSensorsFragment")
//                        //if there is no alarm in process then shut down the timer
//                        if (UserSession.instance.alarmSensors == null
//                            || UserSession.instance.alarmSensors?.isEmpty()!!
//                            || isAllSensorAlarmTimeOutSound()
//                        ) {
//
//                            activity?.let { act ->
//                                ViewModelProviders.of(act).get(ViewModelListener::class.java)
//                                    .shutDownTimer()
//                            }
//                            //showMarkers()
//                        } else {
//                            //set all the time out sensors alarm as sound off
//                            replaceSensorAlarmTimeOutToSensorMarker()
////                            showMarkers()
//                        }
//                        //if the
//                        Log.d(
//                            "testAlarmMap",
//                            "startTimerListener in MapSensorsFragment:showMarkers"
//                        )
//                        showMarkers()
//
//                    })
//
//        }
//    }

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


//    private fun showDialogSensorsList() {
//
//        //TODO to separate the adapters
//
//        var sensors = activity?.let { getSensorsFromLocally(it) }
//        sensors = sortByIdAlarm(sensors)?.let { ArrayList(it) }
//
//        if (dialog != null && dialog?.isShowing!!) {
//            sensorsDialogAdapter?.setDetects(sensors)
//            sensorsDialogAdapter?.notifyDataSetChanged()
//            return
//        }
//
//        sensorsDialogAdapter = activity?.let { adapter ->
//            sensors?.let { arr ->
//                SensorsDialogAdapter(arr, adapter, this) { _ ->
//
//                }
//            }
//        }
//
//        //create dialog
//        dialog = this.context?.let { Dialog(it) }
//        //set layout custom
//        dialog?.setContentView(R.layout.dialog_list_detectors)
//
//        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
//        val height = (resources.displayMetrics.heightPixels * 0.75).toInt()
//        dialog?.window?.setLayout(width, height)
//
//        val rvDetector = dialog?.findViewById<RecyclerView>(R.id.rvDetector)
//        val btnSaveLocateSensor = dialog?.findViewById<Button>(R.id.btnSaveLocateSensor)
//        btnSaveLocateSensor?.setOnClickListener {
//            SensorsDialogAdapter.selectedSensor
//
//            currentLatitude?.let { SensorsDialogAdapter.selectedSensor?.setLatitude(it) }
//            currentLongitude?.let { SensorsDialogAdapter.selectedSensor?.setLongtitude(it) }
//
//            SensorsDialogAdapter.selectedSensor?.let { sensor -> saveLatLongDetector(sensor) }
//            dialog?.dismiss()
//            showMarkers()
//
//        }
//
//        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
//        itemDecorator.setDrawable(
//            ContextCompat.getDrawable(
//                requireContext(),
//                R.drawable.divider
//            )!!
//        )
//        rvDetector?.addItemDecoration(itemDecorator)
//
//        sensorsDialogAdapter?.itemClick = { detector ->
//
//        }
//
//        // Add some item here to show the list.
//        rvDetector?.adapter = sensorsDialogAdapter
//        val mLayoutManager = LinearLayoutManager(context)
//        rvDetector?.layoutManager = mLayoutManager
//        dialog?.show()
//    }

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
    }

//        val alarmSensor = UserSession.instance.alarmSensors
//        val iteratorList = alarmSensor?.listIterator()
//        while (iteratorList != null && iteratorList.hasNext()) {
//            val sensorItem = iteratorList.next()
//            if (sensorItem.latitude != null
//                && sensorItem.longitude != null
//            ) {
//                if (sensorItem != null) {
//
//                    //if time out then remove the sensor from alarm list
////                    if (isSensorAlarmTimeout(sensorItem)) {
////                        iteratorList.remove()
////                    } else {
//                    //save the marker for update after timeout
//                    sensorItem.markerFeature = showSensorAlarmMarker(
//                        sensorItem,
//                        sensorItem.type,
//                        sensorItem.typeIdx,
//                        sensorItem.zone
//                    )
////                    }
//
//                } else {
//                    //show sensor marker
//                    showSensorMarker(sensorItem)
//                }
//            }
//        }
//
//    }
    //get sensors from locally
//        val sensorsArr = activity?.let { getSensorsFromLocally(it) }
//
//        // scan all saved (with locations) sensors
//        val iteratorList = sensorsArr?.listIterator()
//        while (iteratorList != null && iteratorList.hasNext()) {
//            val sensorItem = iteratorList.next()
//            if (sensorItem.getLatitude() != null
//                && sensorItem.getLongtitude() != null
//            ) {
//
//                val sensorAlarm = getSensorAlarmBySensor(sensorItem)
//
//                if (sensorAlarm != null) {
//
//                    //if time out then remove the sensor from alarm list
//                    if (isSensorAlarmTimeout(sensorAlarm)) {
//                        UserSession.instance.alarmSensors?.remove(sensorAlarm)
//                        showSensorMarker(sensorItem)
//                    } else {
//                        //save the marker for update after timeout
//                        sensorAlarm.markerFeature = showSensorAlarmMarker(
//                            sensorItem,
//                            sensorAlarm.type,
//                            sensorAlarm.typeIdx
//                        )
//                    }
//
//                } else {
//                    //show one sensor marker
//                    showSensorMarker(sensorItem)
//                }
//
//            }
//        }
//    }


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

//    /**
//     * show marker of sensor alarm
//     */
//    private fun showSensorAlarmMarker(sensorItem: AlarmSensor, type: String, typeIdx: Int?): Feature? {
//
//        if (mapView == null) {
//            return null
//        }
//
//        val loc: LatLng =
//            LatLng(
//                sensorItem.latitude!!,
//                sensorItem.latitude!!
//            )
//
//        var alarmTypeIcon: Feature? = null
//
//        //car ,intruder and off are relevant when type = seismic
//        if (sensorItem.type == SEISMIC_TYPE) {
//            //set icon according to type alarm
//            alarmTypeIcon =
//                when (typeIdx) {
//                    ALARM_CAR -> {
//                        loc.let { addMarker(it, CAR_ICON_ID, sensorItem.getName(), type) }
//                    }
//                    ALARM_INTRUDER -> {
//                        loc.let { addMarker(it, INTRUDER_ICON_ID, sensorItem.getName(), type) }
//                    }
//                    ALARM_SENSOR_OFF -> {
//                        loc.let { addMarker(it, SENSOR_OFF_ICON_ID, sensorItem.getName(), type) }
//                    }
//                    ALARM_LOW_BATTERY -> {
//                        loc.let { addMarker(it, LOW_BATTERY_ICON_ID, sensorItem.getName(), type) }
//                    }
//                    else -> {
//                        loc.let { addMarker(it, RED_ICON_ID, sensorItem.getName(), type) }
//                    }
//                }
//        }// RADAR,PIR,VIBRATION
//        else if (typeIdx == ALARM_CAR
//            || typeIdx == ALARM_INTRUDER
//            || typeIdx == ALARM_MOTION
//            || typeIdx == ALARM_SENSOR_OFF
//        ) {
//
//            alarmTypeIcon =
//                when (sensorItem.getTypeID()) {
//                    PIR_TYPE -> loc.let {
//                        addMarker(
//                            it,
//                            PIR_ICON_ID,
//                            sensorItem.getName(),
//                            sensorItem.getType()
//                        )
//                    }
//
//                    RADAR_TYPE -> loc.let {
//                        addMarker(
//                            it,
//                            RADAR_ICON_ID,
//                            sensorItem.getName(),
//                            sensorItem.getType()
//                        )
//                    }
//
//                    VIBRATION_TYPE -> loc.let {
//                        addMarker(
//                            it,
//                            VIBRATION_ICON_ID,
//                            sensorItem.getName(),
//                            sensorItem.getType()
//                        )
//                    }
//
//                    else -> {
//                        loc.let {
//                            addMarker(
//                                it,
//                                RED_ICON_ID,
//                                sensorItem.getName(),
//                                sensorItem.getType()
//                            )
//                        }
//                    }
//                }
//        }// RADAR,PIR,VIBRATION
//        else if (typeIdx == ALARM_LOW_BATTERY
//            || typeIdx == ALARM_KEEP_ALIVE
//            || typeIdx == ALARM_DUAL_TECH
//        ) {
//
//            loc.let {
//                addMarker(
//                    it,
//                    RED_ICON_ID,
//                    sensorItem.getName(),
//                    sensorItem.getType()
//                )
//            }
//        }
//
//
//
//        return alarmTypeIcon
//    }

    /**
     * add one marker to the map
     */
    private fun addMarker(
        location: LatLng,
        iconId: String,
        cameraName: String?,
        type: String?
    ): Feature? {

        var myIcon: Int? = null

        when (iconId) {
            GREEN_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_item
            }

            CAR_ICON_ID -> {
                myIcon = R.drawable.ic_alarm_car
            }

            INTRUDER_ICON_ID -> {
                myIcon = R.drawable.ic_alarm_intruder
            }

            GRAY_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_item_disable
            }

            SENSOR_OFF_ICON_ID -> {
                myIcon = R.drawable.ic_alarm_sensor_off
            }

            PIR_ICON_ID -> {
                myIcon = R.drawable.ic_pir
            }

            RADAR_ICON_ID -> {
                myIcon = R.drawable.ic_radar
            }

            VIBRATION_ICON_ID -> {
                myIcon = R.drawable.ic_vibration
            }

            RED_ICON_ID -> {
                myIcon = R.drawable.ic_sensor_alarm
            }

            LOW_BATTERY_ICON_ID -> {
                myIcon = R.drawable.ic_alarm_low_battery
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
        pointAnnotationOptions.textField = "$cameraName:$type"

        //set transparent to hide preview text (without click)
        //TODO to learn how to hide annotation view text
        pointAnnotationOptions.withTextColor(Color.TRANSPARENT)
        // Add the resulting pointAnnotation to the map.
        pointAnnotationManager?.create(pointAnnotationOptions)
        pointAnnotationManager?.addClickListener(object : OnPointAnnotationClickListener {
            override fun onAnnotationClick(annotation: PointAnnotation): Boolean {


                // Remove existing popup if any
//                viewAnnotationManager?.removeAllViewAnnotations()
//
//                currentPopup = createPopup(annotation)
//
//
//                val options: ViewAnnotationOptions?
//                options = viewAnnotationOptions {
//                    geometry(
//                        Point.fromLngLat(
//                            annotation.point.longitude(),
//                            annotation.point.latitude()
//                        )
//                    )
//                    allowOverlap(false)
//                    annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
//                    //visible(false)
//                }
//
//                val lp = LinearLayout.LayoutParams(
//                    WRAP_CONTENT,
//                    WRAP_CONTENT
//                )
//                currentPopup?.layoutParams = lp
//
//
//                if (currentPopup != null) {
//                    // Add the popup as a ViewAnnotation
//                    viewAnnotationManager?.addViewAnnotation(currentPopup!!, options)
//                }


                return true
            }
        })
        return null
    }


    override fun saveNameSensor(detector: Sensor) {}

    override fun saveSensors(detector: Sensor) {}

    override fun onMove(detector: MoveGestureDetector): Boolean {
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {}

    override fun onMoveEnd(detector: MoveGestureDetector) {}

}