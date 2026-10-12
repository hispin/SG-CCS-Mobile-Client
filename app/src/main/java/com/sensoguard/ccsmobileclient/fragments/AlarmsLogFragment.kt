package com.sensoguard.ccsmobileclient.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.adapters.AlarmAdapter
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.classes.SystemSort
import com.sensoguard.ccsmobileclient.global.CAMERA_KEY
import com.sensoguard.ccsmobileclient.global.CAMERA_SORTED
import com.sensoguard.ccsmobileclient.global.CREATE_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.DATE_SORTED
import com.sensoguard.ccsmobileclient.global.FROM_CALENDAR
import com.sensoguard.ccsmobileclient.global.NO_SORTED
import com.sensoguard.ccsmobileclient.global.RESULT_CODE
import com.sensoguard.ccsmobileclient.global.SORT_BY_DATETIME_KEY
import com.sensoguard.ccsmobileclient.global.SORT_BY_SYSTEM_KEY
import com.sensoguard.ccsmobileclient.global.SORT_BY_SYSTEM_REQUEST_CODE
import com.sensoguard.ccsmobileclient.global.SORT_PICK_DATE_TIME_REQUEST_CODE
import com.sensoguard.ccsmobileclient.global.SORT_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.TO_CALENDAR
import com.sensoguard.ccsmobileclient.global.alarmsListToCsvFile
import com.sensoguard.ccsmobileclient.global.convertJsonToSystemSortList
import com.sensoguard.ccsmobileclient.global.populateAlarmsFromLocally
import com.sensoguard.ccsmobileclient.global.shareCsv
import com.sensoguard.ccsmobileclient.global.storeAlarmsToLocally
import com.sensoguard.ccsmobileclient.global.writeCsvFile
import java.util.Calendar

class AlarmsLogFragment : ParentFragment() {

    private var myAlarms: ArrayList<Alarm>? = null
    private var mySortedAlarms: ArrayList<Alarm>? = null
    private var rvAlarm: RecyclerView? = null
    private var alarmAdapter: AlarmAdapter? = null
    private var btnCsv: Button? = null

    private var btnFilterSystem: Button? = null
    private var btnFilterDateTime: Button? = null
    private var cbIsSelected: CheckBox? = null
    private var ibDeleteSelectedItems: ImageButton? = null
    private var tvReset: TextView? = null

    private var mySortedCameras: ArrayList<SystemSort>? = null
    private var fromCalendar: Calendar? = null
    private var toCalendar: Calendar? = null
    private var typeOfSorted: Int = NO_SORTED


    private fun initAlarmsAdapter() {
        myAlarms = ArrayList()
        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)
            ?.let { itemDecorator.setDrawable(it) }
        rvAlarm?.addItemDecoration(itemDecorator)

        alarmAdapter = myAlarms?.let { arr -> AlarmAdapter(arr, requireActivity()) { } }
        rvAlarm?.adapter = alarmAdapter
        rvAlarm?.layoutManager = LinearLayoutManager(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_alarm_log, container, false)

        rvAlarm = view.findViewById(R.id.rvAlarm)
        initAlarmsAdapter()

        btnCsv = view.findViewById(R.id.btnCsv)
        btnCsv?.setOnClickListener {
            exportAlarmsToCsv()
        }

        btnFilterSystem = view.findViewById(R.id.btnFilterSystem)
        btnFilterSystem?.setOnClickListener {
            setUIBeforeSorting(it as Button)
            openSortByType(SORT_BY_SYSTEM_KEY, SORT_BY_SYSTEM_REQUEST_CODE)
        }

        btnFilterDateTime = view.findViewById(R.id.btnFilterDateTime)
        btnFilterDateTime?.setOnClickListener {
            setUIBeforeSorting(it as Button)
            openSortByType(SORT_BY_DATETIME_KEY, SORT_PICK_DATE_TIME_REQUEST_CODE)
        }

        cbIsSelected = view.findViewById(R.id.cbIsSelected)
        cbIsSelected?.setOnCheckedChangeListener { _, isChecked ->
            getShownAlarms()?.let { toggleItemSelected(it, isChecked) }
        }

        ibDeleteSelectedItems = view.findViewById(R.id.ibDeleteSelectedItems)
        ibDeleteSelectedItems?.setOnClickListener {
            val alarmCounter = getShownAlarms()?.count { it.isReadyToDelete } ?: 0
            if (alarmCounter > 0) {
                showDeleteDialog(alarmCounter)
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.no_selected_alarms),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tvReset = view.findViewById(R.id.tvReset)
        tvReset?.let { Linkify.addLinks(it, Linkify.WEB_URLS) }
        tvReset?.setOnClickListener {
            typeOfSorted = NO_SORTED
            refreshAlarmsFromPref()
        }
        typeOfSorted = NO_SORTED

        // Inflate the layout for this fragment
        return view
    }

    //the alarms that are shown now (sorted or all of them)
    private fun getShownAlarms(): ArrayList<Alarm>? {
        return if (typeOfSorted == DATE_SORTED || typeOfSorted == CAMERA_SORTED) {
            mySortedAlarms
        } else {
            myAlarms
        }
    }

    //write the alarms to csv file and share it
    private fun exportAlarmsToCsv() {
        val activity = activity ?: return
        val alarmsRows = alarmsListToCsvFile(populateAlarmsFromLocally(activity), activity)
        if (writeCsvFile(alarmsRows, activity)) {
            shareCsv(activity)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    //clear the selection of the sorted and normal array
    private fun clearSelection() {
        cbIsSelected?.isChecked = false
        mySortedAlarms?.let { toggleItemSelected(it, false) }
        myAlarms?.let { toggleItemSelected(it, false) }
    }


    //toggle selected/unselected alarms
    private fun toggleItemSelected(alarms: ArrayList<Alarm>, isSelected: Boolean) {
        alarms.forEach { it.isReadyToDelete = isSelected }
        alarmAdapter?.setDetects(alarms)
    }


    override fun onStart() {
        super.onStart()
        setFilter()
        refreshAlarmsFromPref()
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(usbReceiver)
    }

    private fun refreshAlarmsFromPref() {
        val alarms = populateAlarmsFromLocally(context) ?: return
        myAlarms = ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))

        when (typeOfSorted) {
            DATE_SORTED -> {
                sortByDateAlarm()
                alarmAdapter?.setDetects(mySortedAlarms)
            }

            CAMERA_SORTED -> {
                sortByCamerasAlarm()
                alarmAdapter?.setDetects(mySortedAlarms)
            }

            else -> alarmAdapter?.setDetects(myAlarms)
        }
    }

    //sort the alarm by date time
    private fun sortByDateAlarm() {
        val from = fromCalendar?.timeInMillis ?: return
        val to = toCalendar?.timeInMillis ?: return
        mySortedAlarms = myAlarms?.filterTo(ArrayList()) {
            val time = it.timeInMillis
            time != null && time in from..to
        }
    }

    //sort the alarm by sorter camera
    private fun sortByCamerasAlarm() {
        val sortedCameras = mySortedCameras ?: return
        mySortedAlarms = myAlarms?.filterTo(ArrayList()) { isAlarmSorted(it, sortedCameras) }
    }


    //check if the the alarm is sorted
    private fun isAlarmSorted(alarm: Alarm, systemSort: ArrayList<SystemSort>): Boolean {
        return systemSort.any { alarm.zone == it.unit && it.isSorted == true }
    }

    private fun setFilter() {
        ContextCompat.registerReceiver(
            requireActivity(),
            usbReceiver,
            IntentFilter(CREATE_ALARM_KEY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == CREATE_ALARM_KEY) {
                refreshAlarmsFromPref()
            }
        }
    }

    //open fragment dialog to sort the list of alarm log
    private fun openSortByType(type: Int, requestCode: String) {

        val fr = SystemSortDialogFragment()

        val bdl = Bundle()
        bdl.putInt(SORT_TYPE_KEY, type)
        fr.arguments = bdl

        // In the parent fragment
        parentFragmentManager.setFragmentResultListener(
            requestCode,
            viewLifecycleOwner
        ) { _, bundle ->
            val resultCode = bundle.getInt(RESULT_CODE)

            setUIAfterSorting()

            if (resultCode != Activity.RESULT_OK) {
                return@setFragmentResultListener
            }

            if (requestCode == SORT_BY_SYSTEM_REQUEST_CODE) {
                bundle.getString(CAMERA_KEY)?.let {
                    mySortedCameras = convertJsonToSystemSortList(it)
                }
                if (mySortedCameras != null) {
                    typeOfSorted = CAMERA_SORTED
                    refreshAlarmsFromPref()
                }
            } else if (requestCode == SORT_PICK_DATE_TIME_REQUEST_CODE) {
                //get the start date and end date for sorting
                fromCalendar = BundleCompat.getSerializable(bundle, FROM_CALENDAR, Calendar::class.java)
                toCalendar = BundleCompat.getSerializable(bundle, TO_CALENDAR, Calendar::class.java)

                if (fromCalendar != null && toCalendar != null) {
                    typeOfSorted = DATE_SORTED
                    refreshAlarmsFromPref()
                } else {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        parentFragmentManager.beginTransaction()
            .add(R.id.flSortBySystemCamera, fr)
            .commit()
    }

    /**
     * set UI before sorting (mark the pressed button and disable the sort buttons)
     */
    private fun setUIBeforeSorting(pressedButton: Button) {
        //clear if selected
        clearSelection()

        val context = context ?: return
        pressedButton.setBackgroundColor(ContextCompat.getColor(context, R.color.green2))
        pressedButton.setTextColor(ContextCompat.getColor(context, R.color.white))

        btnFilterSystem?.isEnabled = false
        btnFilterDateTime?.isEnabled = false
    }

    /**
     * set UI after sorting
     */
    private fun setUIAfterSorting() {
        btnFilterSystem?.isEnabled = true
        btnFilterDateTime?.isEnabled = true

        //change the color of the buttons
        val context = context ?: return
        val backgroundColor = ContextCompat.getColor(context, R.color.gray11)
        val textColor = ContextCompat.getColor(context, R.color.black)
        btnFilterSystem?.setBackgroundColor(backgroundColor)
        btnFilterSystem?.setTextColor(textColor)
        btnFilterDateTime?.setBackgroundColor(backgroundColor)
        btnFilterDateTime?.setTextColor(textColor)
    }

    //show dialog before delete alarms
    private fun showDeleteDialog(alarmCounter: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(alarmCounter.toString() + " " + requireContext().resources.getString(R.string.selected_alarms))
        val yes = requireContext().resources.getString(R.string.yes)
        val no = requireContext().resources.getString(R.string.no)
        builder.setMessage(requireContext().resources.getString(R.string.do_you_really_want_delete_selected_alarm))
            .setCancelable(false)
        builder.setPositiveButton(yes) { dialog, _ ->

            //delete from main array and also from sort array
            myAlarms?.removeAll { it.isReadyToDelete }
            mySortedAlarms?.removeAll { it.isReadyToDelete }
            //save the changing in shared preference
            myAlarms?.let { storeAlarmsToLocally(it, context) }

            clearSelection()

            refreshAlarmsFromPref()

            dialog.dismiss()
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton(no) { dialog, _ ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }
}
