package com.sensoguard.ccsmobileclient.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.CryptoHandler
import com.sensoguard.ccsmobileclient.global.ACTIVATION_CODE_KEY
import com.sensoguard.ccsmobileclient.global.IMEI_KEY
import com.sensoguard.ccsmobileclient.global.NO_DATA
import com.sensoguard.ccsmobileclient.global.getStringInPreference

class InitAppActivity : ParentActivity() {

    private var myImei: String? = null

    //continue to the activation also when the notifications are not allowed
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            configureActivation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_app)

        checkNotificationPermission()
    }


    /**
     * ask for notification post permission (android 13 and above)
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            configureActivation()
        }
    }


    //get the id of the device and check it with the locally
    private fun configureActivation() {

        myImei = getAndroidId()

        val localActivateCode = getStringInPreference(this, ACTIVATION_CODE_KEY, NO_DATA)
        if (localActivateCode != null && localActivateCode != NO_DATA) {
            val myActivateCode = CryptoHandler.getInstance().encrypt(myImei)
            if (myActivateCode.startsWith(localActivateCode)) {
                val inn = Intent(this, MainActivity::class.java)
                inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(inn)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.wrong_activate_code),
                    Toast.LENGTH_SHORT
                ).show()
                openActivation()
            }
        } else {
            openActivation()
        }
    }

    //open activation screen
    private fun openActivation() {
        val inn = Intent(this, ActivationActivity::class.java)
        inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        inn.putExtra(IMEI_KEY, myImei)
        startActivity(inn)
    }


    /**
     * get the id of the device (the activation code is created from this id).
     * the IMEI is not available for apps since android 10, so the id of the device is used
     */
    //the activation of the app is bound to the device
    @SuppressLint("HardwareIds")
    private fun getAndroidId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
