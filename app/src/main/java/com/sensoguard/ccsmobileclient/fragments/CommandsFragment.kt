package com.sensoguard.ccsmobileclient.fragments

//import com.sensoguard.ccsmobileclient.services.TimerService
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.adapters.CommandAdapter
import com.sensoguard.ccsmobileclient.classes.Command
import com.sensoguard.ccsmobileclient.classes.Sensor
import com.sensoguard.ccsmobileclient.global.*
import com.sensoguard.ccsmobileclient.services.TimerService
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommandsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommandsFragment : DialogFragment() {

    //private var isConnected: Boolean=false
    private var statusAwake = NONE_AWAKE

    //private var myCommand: Command? = null
    private var sensorsIds = ArrayList<String>()
    private var spSensorsIds: AppCompatSpinner? = null
    private var commandsAdapter: CommandAdapter? = null
    private var rvCommands: RecyclerView? = null
    private var btnConnect: Button? = null
    //private var tvTest: TextView? = null

    var selectedSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.getStringArrayList(SENSORS_IDS) != null) {
                sensorsIds.add(resources.getString(R.string.select_sensor))
                sensorsIds.addAll(it.getStringArrayList(SENSORS_IDS)!!)
            }
            Log.d("", "")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_commands_dialog, container, false)
        initViews(view)
        spSensorsIds = view.findViewById(R.id.spSensorsIds)

        //listener for gender selection
        spSensorsIds?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                activity?.sendBroadcast(Intent(STOP_TIMER))
                var item = parent?.getItemAtPosition(position) as String
                getSelectedSensor(item)
                refreshCommandsAdapter()
            }
        }



        if (spSensorsIds != null) {
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                sensorsIds
            )
            spSensorsIds?.adapter = adapter

            // Spinner click listener
            //spinner.setOnItemSelectedListener(this)
        }

        return view
    }

    //get the sensor by the selected item
    private fun getSelectedSensor(id: String) {
        var sensors = ArrayList<Sensor>()
        //sensors?.add(Sensor(resources.getString(R.string.id_title),resources.getString(R.string.name_title)))
        val sensorsListStr = getStringInPreference(activity, DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        if (sensorsListStr.equals(ERROR_RESP)) {
            //ArrayList()
        } else {
            sensorsListStr?.let {
                val temp = convertJsonToSensorList(it)
                temp?.let { tmp -> sensors.addAll(tmp) }
            }
        }

        val items = sensors.listIterator()
        while (items.hasNext()) {
            val item = items.next()
            if (item.getId() == id) {
                selectedSensor = item
            }
        }
    }

    //hag
    var count = 0

    private fun initViews(view: View?) {
        //tvTest = view?.findViewById(R.id.tvTest)
        rvCommands = view?.findViewById(R.id.rvCommands)
        btnConnect = view?.findViewById(R.id.btnConnect)
        btnConnect?.setOnClickListener {
            //hag
            //activity?.sendBroadcast(Intent(STOP_READ_DATA_KEY))
            if (statusAwake == NONE_AWAKE) {

                val isConnected = getBooleanInPreference(activity, USB_DEVICE_CONNECT_STATUS, false)
                //if the usb is connected then open dialog of commands
                //hag
                if (isConnected) {
                    //max timer in seconds
                    count = 0
                    sendSetRefTimer(3, 240, WAIT_AWAKE)
                } else {
                    showToast(activity, resources.getString(R.string.usb_is_disconnect))
                }
            }
        }
    }

    // send set ref timer command
    private fun sendSetRefTimer(timerValue: Int, maxTimeout: Int, status: Int) {

        if (spSensorsIds?.selectedItem.toString() == resources.getString(R.string.select_sensor)) {
            showToast(activity, resources.getString(R.string.no_selected_sensor))
        } else {
            try {
                val id = Integer.parseInt(spSensorsIds?.selectedItem.toString())
                val cmdSetRefTimer: IntArray = intArrayOf(2, id, SET_RF_ON_TIMER, 7, 45, 0, 3)
                UserSession.instance.myCommand = Command(
                    resources.getString(R.string.set_ref_timer),
                    cmdSetRefTimer,
                    R.drawable.ic_parameters
                )
                UserSession.instance.myCommand?.maxTimeout = maxTimeout

                //start timer every 3 second and stop after 30 seconds
                startTimerService(true, timerValue, maxTimeout)

                if (UserSession.instance.myCommand != null) {
                    sendCommand(UserSession.instance.myCommand!!)
                }

                statusAwake = status

                if (statusAwake == WAIT_AWAKE) {
                    btnConnect?.text = resources.getString(R.string.try_connect)
                } else if (statusAwake == OK_AWAKE) {
                    btnConnect?.text = resources.getString(R.string.connected)
                }

                //isConnected=true

            } catch (ex: NumberFormatException) {
                showToast(activity, ex.message.toString())
            }
        }

    }

    //start timer
    fun startTimerService(isRepeated: Boolean, timerValue: Int, maxTimeout: Int?) {
        val intent = Intent(requireContext(), TimerService::class.java)
        intent.putExtra(COMMAND_TYPE, resources.getString(R.string.set_ref_timer))
        intent.putExtra(IS_REPEATED, isRepeated)
        intent.putExtra(TIMER_VALUE, timerValue)
        intent.putExtra(MAX_TIMEOUT, maxTimeout)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(intent)
        } else {
            activity?.startService(intent)
        }
    }


    override fun onStart() {
        super.onStart()
        setFilter()
        //refreshCommandsAdapter()

        //var state = getStringInPreference(activity, "connState", "-1")
        //tvTest?.text = state
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(usbReceiver)
    }

    private fun refreshCommandsAdapter() {

        val commands: ArrayList<Command> = ArrayList()

        //commands of seismic
        if (selectedSensor?.getTypeID() == SEISMIC_TYPE) {

            val cmdGetSens: IntArray = intArrayOf(2, -1, 55, 6, 0, 3)

            commands.add(
                Command(
                    resources.getString(R.string.get_sens_level),
                    cmdGetSens,
                    R.drawable.ic_parameters
                )
            )

            val cmdSetSens: IntArray = intArrayOf(2, -1, 155, 7, -1, -1, 3)

            commands.add(
                Command(
                    resources.getString(R.string.set_sens_level),
                    cmdSetSens,
                    R.drawable.ic_parameters
                )
            )
        }
        commandsAdapter = CommandAdapter(commands, requireContext()) { command: Command ->


            val isConnected = getBooleanInPreference(activity, USB_DEVICE_CONNECT_STATUS, false)

            //if the usb is connected then open dialog of commands
            if (isConnected) {

                //check if sensor has been responded and it has been awake
                if (statusAwake != OK_AWAKE) {
                    showToast(activity, resources.getString(R.string.no_response_sensor))
                    return@CommandAdapter
                }

                //check if sensor has been responded and it has been awake
                if (UserSession.instance.myCommand?.state == PROCESS_STATE) {
                    showToast(activity, resources.getString(R.string.another_process))
                    return@CommandAdapter
                }

                //check if select sensor
                if (spSensorsIds?.selectedItem.toString() == resources.getString(R.string.select_sensor)) {
                    showToast(activity, resources.getString(R.string.no_selected_sensor))
                } else {
                    UserSession.instance.myCommand = command
                    if (command.commandContent?.size != null
                        && command.commandContent.size > 1
                    ) {

                        //start timer
                        startTimerService(false, 4, -1)


                        //set the sensor id
                        command.commandContent[1] =
                            Integer.parseInt(spSensorsIds?.selectedItem.toString())

                        //start progress bar
                        UserSession.instance.myCommand?.state = PROCESS_STATE
                        commandsAdapter?.notifyDataSetChanged()


                        sendCommand(command)
                    }
                }
            } else {
                showToast(activity, resources.getString(R.string.usb_is_disconnect))
            }

        }

        rvCommands?.adapter = commandsAdapter
        val layoutManager = LinearLayoutManager(activity)
        rvCommands?.layoutManager = layoutManager

        commandsAdapter?.notifyDataSetChanged()

    }

    //send rf timer command immediately after other command
    private fun sendRfCmd() {
        val id = Integer.parseInt(spSensorsIds?.selectedItem.toString())
        val cmdSetRefTimer: IntArray = intArrayOf(2, id, SET_RF_ON_TIMER, 7, 45, 0, 3)
        val command = Command(
            resources.getString(R.string.set_ref_timer),
            cmdSetRefTimer,
            R.drawable.ic_parameters
        )
        sendCommand(command)
    }

    //sen command to sensor
    private fun sendCommand(command: Command) {


        //val inn = Intent(ACTION_SEND_CMD)
        //val bnd = Bundle()

        UserSession.instance.commandContent = command.commandContent

        //bnd.putIntArray(CURRENT_COMMAND, command.commandContent)

        //hag
        //Log.d("TestCommand","send command")
        //count++
        //showToast(activity, count.toString()+ " array size="+command.commandContent?.size)
        //showToast(activity, command.commandContent!![1].toString())
        //inn.replaceExtras(bnd)
        activity?.sendBroadcast(Intent(ACTION_SEND_CMD))
    }


    fun collapseExpandTextView(view: View) {
        if (view.visibility == View.GONE) {
            // it's collapsed - expand it
            view.visibility = View.VISIBLE
        } else {
            // it's expanded - collapse it
            view.visibility = View.GONE
        }
    }

    private fun setFilter() {
        val filter = IntentFilter("handle.read.data")
        filter.addAction(ACTION_USB_RESPONSE_CACHE)
        filter.addAction(ACTION_INTERVAL)
        filter.addAction(MAX_TIMER_RESPONSE)
        filter.addAction("test.brod")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            activity?.registerReceiver(usbReceiver, filter)
        }

    }
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            if (inn.action == ACTION_USB_RESPONSE_CACHE) {
                val arr = inn.getIntegerArrayListExtra(USB_CACHE_RESPONSE_KEY)

                //response of get sens command
                if (arr != null && arr.size > 5 && arr[2].toUByte().toInt() == GET_SENS_LEVEL) {
                    if (UserSession.instance.myCommand?.state == PROCESS_STATE) {
                        //stop progress bar
                        UserSession.instance.myCommand?.state = SUCCESS_STATE
                        commandsAdapter?.notifyDataSetChanged()
                        showResponseInDialog(arr[4], arr[5])
                    }
                    //response of set sens command
                } else if (arr != null && arr.size == 7 && arr[2].toUByte()
                        .toInt() == SET_SENS_LEVEL
                ) {
                    if (UserSession.instance.myCommand?.state == PROCESS_STATE) {
                        //stop progress bar
                        UserSession.instance.myCommand?.state = SUCCESS_STATE
                        commandsAdapter?.notifyDataSetChanged()
                    }
                    //accept response from command of RF timer
                } else if (arr != null && arr.size == 7 && arr[2].toUByte()
                        .toInt() == SET_RF_ON_TIMER
                //&& arr[4].toUByte().toInt() == 1
                ) {
                    statusAwake = OK_AWAKE
                    btnConnect?.text = resources.getString(R.string.connected)
                    startTimerService(true, 20, -1)

                }
                //time out (no max)
            } else if (inn.action == ACTION_INTERVAL) {
                val commandType = inn.getStringExtra(COMMAND_TYPE)

                //if the interval is belong to the command "set ref timer"
                //do nothing ,ServiceConnectSensor also handle it
                if (commandType != null && commandType == resources.getString(R.string.set_ref_timer)) {

                    //showShortToast(activity, "interval")
                    //do nothing ,ServiceConnectSensor also handle it

                    //accept interval after other commands (timeout, not repeated)
                } else if (UserSession.instance.myCommand?.state == PROCESS_STATE) {

                    //stop progress bar
                    UserSession.instance.myCommand?.state = TIMEOUT_STATE
                    commandsAdapter?.notifyDataSetChanged()
                    //showToast(activity, "timeout")

                    //if the sensor awake then renew the normal Timer RF commands timer
                    if (statusAwake == OK_AWAKE) {
                        sendSetRefTimer(20, -1, OK_AWAKE)
                    }
                } else {
                    //renew the normal Timer RF commands timer
                    if (statusAwake == OK_AWAKE) {
                        sendSetRefTimer(20, -1, OK_AWAKE)
                    }
                }
            } else if (inn.action == MAX_TIMER_RESPONSE) {
                btnConnect?.text = resources.getString(R.string.connect)
                statusAwake = NONE_AWAKE
            }
            //hag test
//            else if (inn.action == "test.brod") {
//                   //count++
//                   val s=  inn.getIntExtra("size", -1)
//                   val appcode=  inn.getIntExtra("appcode", -1)
//
//                   showToast(activity, "size=$s appcode=$appcode")
//            }
        }
        }

        //show dialog to show response
        private fun showResponseInDialog(carV: Int, intruderV: Int) {

            if (this@CommandsFragment.context != null) {
                val dialog = Dialog(this@CommandsFragment.requireContext())
                dialog.setContentView(R.layout.dialog_command_response)

                dialog.setCancelable(true)


                val tvCarValue = dialog.findViewById<AppCompatTextView>(R.id.tvCarValue)
                val tvIntruderValue = dialog.findViewById<AppCompatTextView>(R.id.tvIntruderValue)
                tvCarValue.text = carV.toString()
                tvIntruderValue.text = intruderV.toString()

                val btnClose = dialog.findViewById<AppCompatButton>(R.id.btnClose)
                btnClose.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

    }
