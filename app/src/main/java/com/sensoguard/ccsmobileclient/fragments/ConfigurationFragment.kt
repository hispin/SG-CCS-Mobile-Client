package com.sensoguard.ccsmobileclient.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.activities.DownloadOfflineTilesActivity
import com.sensoguard.ccsmobileclient.adapters.GeneralItemMenuAdapter
import com.sensoguard.ccsmobileclient.classes.*
import com.sensoguard.ccsmobileclient.global.*
import com.sensoguard.ccsmobileclient.interfaces.CallToParentInterface
import com.sensoguard.ccsmobileclient.interfaces.OnFragmentListener


open class ConfigurationFragment : ParentFragment(), CallToParentInterface {


    private var isPasswordVisible: Boolean = false
    private var listPopupWindow: ListPopupWindow? = null
    private var generalItemMenuAdapter: GeneralItemMenuAdapter? = null
    //private var etSensorValue: AppCompatEditText? = null
    //private var btnSaveSensors: AppCompatButton? = null
    private var togChangeAlarmVibrate: ToggleButton? = null
    private var ibSatelliteMode: AppCompatButton? = null
    private var ibNormalMode: AppCompatButton? = null
    private var etAlarmFlickerValue: AppCompatEditText? = null
    private var btnSaveFlicker: AppCompatButton? = null
    private var constAlarmSound: ConstraintLayout? = null
    private var txtAlarmSoundValue: TextView? = null
    private var togChangeAlarmSound: ToggleButton? = null
    private var btnDefault: AppCompatButton? = null

    //private var ivSelectLanguage: AppCompatImageView?=null
    private var constLangView: ConstraintLayout? = null
    private var languageValue: TextView? = null
    private var listener: OnFragmentListener? = null
    //private var btnSaveOffline: AppCompatButton? = null
    private var togIsSensorAlwaysShow: ToggleButton? = null
    private var ibSetEmailDetails: AppCompatImageButton? = null
    private var togForwardSensorEmail: ToggleButton? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnAdapterListener")
        }
    }

    //(activity,ALARM_FLICKERING_DURATION_KEY,ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        //etSensorValue = view.findViewById(R.id.etSensorValue)
        val currentNumSensors = getCurrentNumSensorsFromLocally()
//        if (currentNumSensors != null) {
//            etSensorValue?.setText(currentNumSensors.toString())
//        }

//        btnSaveSensors = view.findViewById(R.id.btnSaveSensors)
//        btnSaveSensors?.setOnClickListener {
//            addSensors()
//        }

        togChangeAlarmVibrate = view.findViewById(R.id.togChangeAlarmVibrate)
        togChangeAlarmVibrate?.isChecked =
            getBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, true)
        togChangeAlarmVibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, isChecked)
        }

        ibSatelliteMode = view.findViewById(R.id.ibSatelliteMode)
        ibSatelliteMode?.setOnClickListener {
            setMapSatellite()
        }

        ibNormalMode = view.findViewById(R.id.ibNormalMode)
        ibNormalMode?.setOnClickListener {
            setMapNormal()
        }
        val mapType = getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)
        if (mapType == MAP_SHOW_NORMAL_VALUE) {
            setMapNormal()
        } else if (mapType == MAP_SHOW_SATELLITE_VALUE) {
            setMapSatellite()
        }

        etAlarmFlickerValue = view.findViewById(R.id.etAlarmFlickerValue)
        etAlarmFlickerValue?.setText(
            getLongInPreference(
                activity,
                ALARM_FLICKERING_DURATION_KEY,
                -1L
            ).toString()
        )

        btnSaveFlicker = view.findViewById(R.id.btnSaveFlicker)
        //update the time flickering
        btnSaveFlicker?.setOnClickListener {
            try {
                val timeFlicker = etAlarmFlickerValue?.text.toString().toLong()
                setLongInPreference(activity, ALARM_FLICKERING_DURATION_KEY, timeFlicker)
                Toast.makeText(
                    activity,
                    resources.getString(com.sensoguard.ccsmobileclient.R.string.time_flickering_save_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (ex: NumberFormatException) {
            }

        }

        constAlarmSound = view.findViewById(R.id.constAlarmSound)
        constAlarmSound?.setOnClickListener {
            openSoundsMenu()
        }
        txtAlarmSoundValue = view.findViewById(R.id.txtAlarmSoundValue)

        var title = getSelectedNotificationSound()
        txtAlarmSoundValue?.text = title

        togChangeAlarmSound = view.findViewById(R.id.togChangeAlarmSound)
        togChangeAlarmSound?.isChecked =
            getBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, true)
        togChangeAlarmSound?.setOnCheckedChangeListener { buttonView, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, isChecked)
        }

        togForwardSensorEmail = view.findViewById(R.id.togForwardSensorEmail)
        togForwardSensorEmail?.isChecked =
            getBooleanInPreference(activity, IS_FORWARD_ALARM_EMAIL, false)
        togForwardSensorEmail?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (checkFilledEmail()) {
                    //update the status of the alarm vibrate : on/off
                    setBooleanInPreference(activity, IS_FORWARD_ALARM_EMAIL, isChecked)
                } else {
                    togForwardSensorEmail?.isChecked =
                        getBooleanInPreference(activity, IS_FORWARD_ALARM_EMAIL, false)
                }
            } else {
                //update the status of the alarm vibrate : on/off
                setBooleanInPreference(activity, IS_FORWARD_ALARM_EMAIL, isChecked)
            }
        }

        togIsSensorAlwaysShow = view.findViewById(R.id.togIsSensorAlwaysShow)
        togIsSensorAlwaysShow?.isChecked =
            getBooleanInPreference(activity, IS_SENSOR_NAME_ALWAYS_KEY, false)
        togIsSensorAlwaysShow?.setOnCheckedChangeListener { buttonView, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity, IS_SENSOR_NAME_ALWAYS_KEY, isChecked)
        }

        btnDefault = view.findViewById(R.id.btnDefault)
        btnDefault?.setOnClickListener {
            //return the alarm to default sound
            val packageName = "android.resource://${activity?.packageName}/raw/alarm_sound"
            val uri = Uri.parse(packageName)
            setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
            title = getSelectedNotificationSound()
            txtAlarmSoundValue?.text = title
        }

//        ivSelectLanguage=view.findViewById(R.id.ivSelectLanguage)
//        ivSelectLanguage?.setOnClickListener{
//            showPopupList(it)
//        }

        constLangView = view.findViewById(R.id.constLangView)
        constLangView?.setOnClickListener {
            showPopupList(it)
        }

        languageValue = view.findViewById(R.id.languageValue)

//        btnSaveOffline = view.findViewById(R.id.btnSaveOffline)
//        btnSaveOffline?.setOnClickListener {
//            startActivity(
//                Intent(
//                    requireActivity(),
//                    DownloadOfflineTilesActivity::class.java
//                )
//            )
//        }
        ibSetEmailDetails = view.findViewById(R.id.ibSetEmailDetails)
        ibSetEmailDetails?.setOnClickListener {
            openSetEmailDetails()
        }


        return view
    }

    //check if the user has been fill the email details
    private fun checkFilledEmail(): Boolean {
        val userName = getStringInPreference(activity, USER_NAME_MAIL, "-1")
        val password = getStringInPreference(activity, PASSWORD_MAIL, "-1")
        val recipient = getStringInPreference(activity, RECIPIENT_MAIL, "-1")
        val server = getStringInPreference(activity, SERVER_MAIL, "-1")
        val port = getIntInPreference(activity, PORT_MAIL, -1)
        val isSSL = getBooleanInPreference(activity, IS_SSL_MAIL, false)

        //check if the account mail has been filled
        if (userName.equals("-1") || password.equals("-1")
            || recipient.equals("-1") || server.equals("-1")
            || port == -1
        ) {
            showToast(activity, resources.getString(R.string.no_fill_account))
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        //set the current language (or selected language or language of device)
        val generalItemMenu = LanguageManager.getCurrentLang(GeneralItemMenu.selectedItem)
        if (generalItemMenu != null) {
            showCurrentLanguage(generalItemMenu)
        }
    }

    //get selected notification sound from locally
    private fun getSelectedNotificationSound(): String? {
        val selectedSound = getStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, "-1")
        if(!selectedSound.equals("-1")) {
            val uri=Uri.parse(selectedSound)
            uri?.let {
                val ringtone = RingtoneManager.getRingtone(activity, uri)
                return ringtone.getTitle(activity)
            }
        }
        return null
    }

    //open menu of notification sounds
    private fun openSoundsMenu(){
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        this.startActivityForResult(intent, 5)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            val uri = intent!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(activity, uri)
                val title = ringtone.getTitle(activity)
                txtAlarmSoundValue?.text = title
                setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
            } else {
                txtAlarmSoundValue?.text = resources.getString(com.sensoguard.ccsmobileclient.R.string.no_selected_sound)
            }
        }
    }

    private fun setMapSatellite() {
        ibNormalMode?.isEnabled = true
        ibSatelliteMode?.isEnabled = false
        setIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
    }


    private fun setMapNormal() {
        ibNormalMode?.isEnabled = false
        ibSatelliteMode?.isEnabled = true
        setIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_NORMAL_VALUE)
    }

    //get the current size of sensors
    private fun getCurrentNumSensorsFromLocally(): Int? {
        val sensors= activity?.let { getSensorsFromLocally(it) }
        return sensors?.size
    }


    //add sensors according to the number that get from user
//    private fun addSensors(){
//
//        var numSensorsRequest:Int?=null
//
//        val sensors= activity?.let { getSensorsFromLocally(it) }
//
//        //check if id is already exist
//        fun isIdExist(sensorsArr: ArrayList<Sensor>, id: String): Boolean {
//            for (item in sensorsArr) {
//                if (item.getId() == id) {
//                    return true
//                }
//            }
//            return false
//        }
//
//        //ask before delete extra sensors
//        fun askBeforeDeleteExtraSensor() {
//            val dialog= AlertDialog.Builder(activity)
//                //set message, title, and icon
//                .setTitle(activity?.resources?.getString(com.sensoguard.ccsmobileclient.R.string.remove_extra_sensors))
//                .setMessage(
//                    activity?.resources?.getString(
//                        com.sensoguard.ccsmobileclient.R.string.content_delete_extra_sensor
//                    )
//                ).setIcon(
//                    android.R.drawable.ic_menu_delete
//
//                )
//
//                .setPositiveButton(activity?.resources?.getString(com.sensoguard.ccsmobileclient.R.string.yes)) { dialog, _ ->
//
//                    //remove extra sensors
//                    if(numSensorsRequest!=null) {
//                        val items=sensors?.listIterator()
//                        while (items != null && items.hasNext()) {
//                            val item = items.next()
//
//                            val id = item.getId()
//                            try {
//                                if (id.toInt() > numSensorsRequest!!) {
//                                    items.remove()
//                                }
//                            } catch (ex: NumberFormatException) {
//                                //do nothing
//                            }
//                        }
//                        Toast.makeText(
//                            activity,
//                            resources.getString(R.string.sensors_save_successfully),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    sensors?.let { sen -> storeSensorsToLocally(sen, requireActivity()) }
//                    dialog.dismiss()
//                }
//
//
//                .setNegativeButton(activity?.resources?.getString(com.sensoguard.ccsmobileclient.R.string.no)) { dialog, _ -> dialog.dismiss() }
//                .create()
//            dialog.show()
//
//        }
//
//
//        try{
//            numSensorsRequest=etSensorValue?.text.toString().toInt()
//        }catch (ex: NumberFormatException){
//            Toast.makeText(this.context, "exception ${ex.message}", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        if(numSensorsRequest!=null
//            && numSensorsRequest >254) {
//            Toast.makeText(
//                this.context,
//                resources.getString(com.sensoguard.ccsmobileclient.R.string.invalid_mum_sensors),
//                Toast.LENGTH_LONG
//            ).show()
//            return
//        }
//
//
//        if(numSensorsRequest!=null) {
//            //add numSensors sensors
//            for (sensorId in 1 until numSensorsRequest + 1) {
//                //add it just if not exist
//                if (sensors?.let { it1 -> !isIdExist(it1, sensorId.toString()) }!!) {
//                    sensors.add(Sensor(sensorId.toString()))
//                }
//            }
//        }
//
//        //check if the request of sensors number is smaller then the number of exist
//        if(sensors?.size!=null
//            && numSensorsRequest!=null
//            && numSensorsRequest < sensors.size){
//            askBeforeDeleteExtraSensor()
//        }else if(activity!=null) {
//            sensors?.let { sen -> storeSensorsToLocally(sen, requireActivity()) }
//            Toast.makeText(
//                activity,
//                resources.getString(R.string.sensors_save_successfully),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//
//    }

    private fun showPopupList(anchorView: View) {
        if (LanguageManager.languagesItems != null && LanguageManager.languagesItems.size > 0) {
            generalItemMenuAdapter = GeneralItemMenuAdapter(
                activity,
                R.layout.row_general_item_menu,
                createLanguagesItemsDeliver(LanguageManager.languagesItems),
                this
            )
            listPopupWindow = context?.let { ListPopupWindow(it) }
            listPopupWindow?.isModal = true
            listPopupWindow?.animationStyle = R.style.winPopupAnimation
            listPopupWindow?.setAdapter(generalItemMenuAdapter)
            listPopupWindow?.anchorView = anchorView
            listPopupWindow?.width = getScreenWidth(activity) * 2 / 3
            listPopupWindow?.show()
        } else {
            Toast.makeText(activity, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    //To prevent two identical list with the same address and depends on each other
    private fun createLanguagesItemsDeliver(items: java.util.ArrayList<GeneralItemMenu>): java.util.ArrayList<GeneralItemMenu> {
        val newItems = java.util.ArrayList<GeneralItemMenu>()
        newItems.addAll(items)
        return newItems
    }

    //select language for app
    override fun selectedItem(item: Any) {
        setStringInPreference(
            activity,
            CURRENT_LANG_KEY_PREF,
            GeneralItemMenu.selectedItem
        )
        if (item is GeneralItemMenu) {
            if (listPopupWindow != null && listPopupWindow!!.isShowing) {
                listPopupWindow?.dismiss()
                if (item != null) {
                    showCurrentLanguage(item)
                }
                listener?.updateLanguage()
            }
        }
    }
    //show current language in menu
    private fun showCurrentLanguage(generalItemMenu: GeneralItemMenu?) {
        if (generalItemMenu != null) {
            languageValue?.text = generalItemMenu.title
//            ibLangSelect.setImageDrawable(
//                ContextCompat.getDrawable(
//                    getMyActivity(),
//                    generalItemMenu.iconLarge
//                )
//            )
        }
    }

    private var dialog: AlertDialog? = null

    //open dialog for log in
    fun openSetEmailDetails() {

        val li: LayoutInflater = LayoutInflater.from(this@ConfigurationFragment.context)
        val promptsView: View = li.inflate(R.layout.custom_account_setting, null)


//        val tvSenderEmail: TextView = promptsView
//            .findViewById(R.id.tvSenderEmail) as TextView
//
//        tvSenderEmail.isSelected = true


        val etUserName: EditText = promptsView
            .findViewById(R.id.etSenderEmail) as EditText

        val etPassword: EditText = promptsView
            .findViewById(R.id.tvIntruderValue) as EditText

        val ibTogglePass: ImageButton = promptsView
            .findViewById(R.id.ibTogglePass) as ImageButton
        ibTogglePass.setOnClickListener {
            togglePassVisibility(etPassword)
        }

        val etMailServer: EditText = promptsView
            .findViewById(R.id.etMailServer) as EditText

        val etMailServerPort: EditText = promptsView
            .findViewById(R.id.etMailServerPort) as EditText

        val etMailRecipient: EditText = promptsView
            .findViewById(R.id.etMailRecipient) as EditText

        val rgIsSSL: RadioGroup = promptsView
            .findViewById(R.id.rgIsSSL) as RadioGroup


        val userName = getStringInPreference(requireContext(), USER_NAME_MAIL, "-1")
        val password = getStringInPreference(requireContext(), PASSWORD_MAIL, "-1")
        val recipient = getStringInPreference(requireContext(), RECIPIENT_MAIL, "-1")
        val server = getStringInPreference(requireContext(), SERVER_MAIL, "-1")
        val port = getIntInPreference(requireContext(), PORT_MAIL, -1)
        val isSsl = getBooleanInPreference(requireContext(), IS_SSL_MAIL, false)

        if (!userName.equals("-1") && !password.equals("-1")
            && !recipient.equals("-1") && !server.equals("-1")
            && port != -1
        ) {
            //showToast(requireContext(),resources.getString(R.string.fill_account))
            etUserName.setText(userName)
            etPassword.setText(password)
            etMailRecipient.setText(recipient)
            etMailServer.setText(server)
            etMailServerPort.setText(port.toString())
            if (isSsl) {
                rgIsSSL.check(R.id.rbYes)
            } else {
                rgIsSSL.check(R.id.rbNo)
            }
        }

        //rgIsSSL.checkedRadioButtonId

//        tvError = promptsView.findViewById<TextView>(R.id.tvError)
//        tvError?.visibility = View.INVISIBLE

        //pbValidation = promptsView.findViewById<ProgressBar>(R.id.pbValidation)

        dialog = AlertDialog.Builder(requireContext())
            .setView(promptsView)
            .setCancelable(false)
            .show()

        val positiveButton = promptsView.findViewById(R.id.btnSave) as Button
        //dialog?.getButton(AlertDialog.BUTTON_POSITIVE)!!
        positiveButton.setOnClickListener(View.OnClickListener {

            if (validIsEmpty(etUserName, requireContext())
                && validIsEmpty(etPassword, requireContext())
                && validIsEmpty(etMailServer, requireContext())
                && validIsEmpty(etMailServerPort, requireContext())
                && validIsEmpty(etMailRecipient, requireContext())

            ) {

                positiveButton.isEnabled = false
                var port: Int? = null
                try {
                    port = etMailServerPort.text.toString().toInt()
                } catch (ex: java.lang.NumberFormatException) {//if enter not number
                    etMailServerPort.error = "enter number"
                    return@OnClickListener
                }
                val myEmailAccount = MyEmailAccount(
                    etUserName.text.toString(), etPassword.text.toString(),
                    etMailServer.text.toString(), port, etMailRecipient.text.toString(),
                    rgIsSSL.checkedRadioButtonId == R.id.rbYes
                )
                saveMyAccount(myEmailAccount)

                //sendEmailBakground()
                dialog?.dismiss()

            }

            //     Toast.makeText(SysManagerActivity.this, "dialog is open", Toast.LENGTH_SHORT).show();
        })
        val negativeButton: Button = promptsView.findViewById(R.id.btnCancel) as Button
        //dialog?.getButton(AlertDialog.BUTTON_POSITIVE)!!
        negativeButton.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
        })
    }

    //toggle password visibility
    private fun togglePassVisibility(etPassword: EditText) {
        if (isPasswordVisible) {
            val pass: String = etPassword.text.toString()
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.setText(pass)
            etPassword.setSelection(pass.length)
        } else {
            val pass: String = etPassword.text.toString()
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            etPassword.inputType = InputType.TYPE_CLASS_TEXT
            etPassword.setText(pass)
            etPassword.setSelection(pass.length)
        }
        isPasswordVisible = !isPasswordVisible
    }

    //save locally the Email account details
    private fun saveMyAccount(myEmailAccount: MyEmailAccount) {
        setStringInPreference(requireContext(), USER_NAME_MAIL, myEmailAccount.userName)
        setStringInPreference(requireContext(), PASSWORD_MAIL, myEmailAccount.password)
        setStringInPreference(requireContext(), SERVER_MAIL, myEmailAccount.outgoingServer)
        myEmailAccount.outgoingPort?.let { setIntInPreference(requireContext(), PORT_MAIL, it) }
        setStringInPreference(requireContext(), RECIPIENT_MAIL, myEmailAccount.recipient)
        setBooleanInPreference(requireContext(), IS_SSL_MAIL, myEmailAccount.isUseSSL)
    }

//    private fun sendEmailBakground() {
//        val auth = EmailService.UserPassAuthenticator("sg-patrol@sgsmtp.com", "SensoGuard1234")//sg-patrol@sgsmtp.com
//        val to = listOf(InternetAddress("hag.swead@gmail.com"))
//        val from = InternetAddress("sg-patrol@sgsmtp.com")
//        val email = EmailService.Email(auth, to, from, "Test Subject to haggay", "Hello Haggay")
//        val emailService = EmailService("mail.sgsmtp.com", 587)
//        //TODO ssl=0
//        //use CoroutineScope to prevent blocking main thread
//        GlobalScope.launch { // or however you do background threads
//            emailService.send(email)
//        }
//    }
}