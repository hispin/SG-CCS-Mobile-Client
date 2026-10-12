package com.sensoguard.ccsmobileclient.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.adapters.GeneralItemMenuAdapter
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu
import com.sensoguard.ccsmobileclient.classes.LanguageManager
import com.sensoguard.ccsmobileclient.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.ccsmobileclient.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.ccsmobileclient.global.FCM_TOKEN_KEY
import com.sensoguard.ccsmobileclient.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.IS_SENSOR_NAME_ALWAYS_KEY
import com.sensoguard.ccsmobileclient.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.ccsmobileclient.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.ccsmobileclient.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.ccsmobileclient.global.getBooleanInPreference
import com.sensoguard.ccsmobileclient.global.getIntInPreference
import com.sensoguard.ccsmobileclient.global.getLongInPreference
import com.sensoguard.ccsmobileclient.global.getScreenWidth
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setBooleanInPreference
import com.sensoguard.ccsmobileclient.global.setIntInPreference
import com.sensoguard.ccsmobileclient.global.setLongInPreference
import com.sensoguard.ccsmobileclient.global.setStringInPreference
import com.sensoguard.ccsmobileclient.global.toastNotify
import com.sensoguard.ccsmobileclient.interfaces.CallToParentInterface
import com.sensoguard.ccsmobileclient.interfaces.OnFragmentListener


open class ConfigurationFragment : ParentFragment(), CallToParentInterface {

    private var listPopupWindow: ListPopupWindow? = null
    private var generalItemMenuAdapter: GeneralItemMenuAdapter? = null
    private var togChangeAlarmVibrate: ToggleButton? = null
    private var ibSatelliteMode: AppCompatButton? = null
    private var ibNormalMode: AppCompatButton? = null
    private var etAlarmFlickerValue: AppCompatEditText? = null
    private var btnSaveFlicker: AppCompatButton? = null
    private var constAlarmSound: ConstraintLayout? = null
    private var txtAlarmSoundValue: TextView? = null
    private var togChangeAlarmSound: ToggleButton? = null
    private var btnDefault: AppCompatButton? = null
    private var btnShareToken: AppCompatButton? = null

    private var constLangView: ConstraintLayout? = null
    private var languageValue: TextView? = null
    private var listener: OnFragmentListener? = null
    private var togIsSensorAlwaysShow: ToggleButton? = null

    //result of the picker of the alarm sound
    private val ringtonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.let {
                    IntentCompat.getParcelableExtra(
                        it,
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                }

                if (uri != null) {
                    setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
                    txtAlarmSoundValue?.text = getSelectedNotificationSound()
                } else {
                    txtAlarmSoundValue?.text = resources.getString(R.string.no_selected_sound)
                }
            }
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        togChangeAlarmVibrate = view.findViewById(R.id.togChangeAlarmVibrate)
        togChangeAlarmVibrate?.isChecked =
            getBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, true)
        togChangeAlarmVibrate?.setOnCheckedChangeListener { _, isChecked ->
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
        when (getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)) {
            MAP_SHOW_NORMAL_VALUE -> setMapNormal()
            MAP_SHOW_SATELLITE_VALUE -> setMapSatellite()
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
            val timeFlicker = etAlarmFlickerValue?.text.toString().toLongOrNull()
            if (timeFlicker == null) {
                etAlarmFlickerValue?.error = resources.getString(R.string.enter_number)
                return@setOnClickListener
            }
            setLongInPreference(activity, ALARM_FLICKERING_DURATION_KEY, timeFlicker)
            Toast.makeText(
                activity,
                resources.getString(R.string.time_flickering_save_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }

        constAlarmSound = view.findViewById(R.id.constAlarmSound)
        constAlarmSound?.setOnClickListener {
            openSoundsMenu()
        }
        txtAlarmSoundValue = view.findViewById(R.id.txtAlarmSoundValue)
        txtAlarmSoundValue?.text = getSelectedNotificationSound()

        togChangeAlarmSound = view.findViewById(R.id.togChangeAlarmSound)
        togChangeAlarmSound?.isChecked =
            getBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, true)
        togChangeAlarmSound?.setOnCheckedChangeListener { _, isChecked ->
            //update the status of the alarm sound : on/off
            setBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, isChecked)
        }

        togIsSensorAlwaysShow = view.findViewById(R.id.togIsSensorAlwaysShow)
        togIsSensorAlwaysShow?.isChecked =
            getBooleanInPreference(activity, IS_SENSOR_NAME_ALWAYS_KEY, false)
        togIsSensorAlwaysShow?.setOnCheckedChangeListener { _, isChecked ->
            //update the status of always show the sensor name : on/off
            setBooleanInPreference(activity, IS_SENSOR_NAME_ALWAYS_KEY, isChecked)
        }

        btnDefault = view.findViewById(R.id.btnDefault)
        btnDefault?.setOnClickListener {
            //return the alarm to default sound
            val uri = "android.resource://${requireActivity().packageName}/${R.raw.alarm_sound}"
            setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri)
            txtAlarmSoundValue?.text = getSelectedNotificationSound()
        }

        btnShareToken = view.findViewById(R.id.btnShareToken)
        btnShareToken?.setOnClickListener {
            shareTokenByEmail()
        }

        constLangView = view.findViewById(R.id.constLangView)
        constLangView?.setOnClickListener {
            showPopupList(it)
        }

        languageValue = view.findViewById(R.id.languageValue)

        return view
    }

    /**
     * share stored token via email
     */
    private fun shareTokenByEmail() {

        val myToken = getStringInPreference(requireActivity(), FCM_TOKEN_KEY, "-1")

        if (myToken.equals("-1")) {
            toastNotify(resources.getString(R.string.no_token), requireActivity())
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TOKEN")
        emailIntent.putExtra(Intent.EXTRA_TEXT, myToken)
        try {
            startActivity(Intent.createChooser(emailIntent, null))
        } catch (ex: ActivityNotFoundException) {
            Log.d(TAG, "Email App not installed", ex)
        }

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
        if (selectedSound != null && selectedSound != "-1") {
            val ringtone = RingtoneManager.getRingtone(activity, selectedSound.toUri())
            return ringtone?.getTitle(activity)
        }
        return null
    }

    //open menu of notification sounds
    private fun openSoundsMenu() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            resources.getString(R.string.select_alarm)
        )
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        ringtonePickerLauncher.launch(intent)
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

    private fun showPopupList(anchorView: View) {
        if (LanguageManager.languagesItems != null && LanguageManager.languagesItems.isNotEmpty()) {
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
    private fun createLanguagesItemsDeliver(items: ArrayList<GeneralItemMenu>): ArrayList<GeneralItemMenu> {
        return ArrayList(items)
    }

    //select language for app
    override fun selectedItem(item: Any) {
        setStringInPreference(
            activity,
            CURRENT_LANG_KEY_PREF,
            GeneralItemMenu.selectedItem
        )
        if (item is GeneralItemMenu) {
            if (listPopupWindow?.isShowing == true) {
                listPopupWindow?.dismiss()
                showCurrentLanguage(item)
                listener?.updateLanguage()
            }
        }
    }

    //show current language in menu
    private fun showCurrentLanguage(generalItemMenu: GeneralItemMenu?) {
        if (generalItemMenu != null) {
            languageValue?.text = generalItemMenu.title
        }
    }

    companion object {
        private const val TAG = "ConfigurationFragment"
        private const val SUPPORT_EMAIL = "Sales@sensoguard.com"
    }
}
