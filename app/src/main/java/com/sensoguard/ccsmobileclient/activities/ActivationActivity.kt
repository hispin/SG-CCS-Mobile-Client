package com.sensoguard.ccsmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.CryptoHandler
import com.sensoguard.ccsmobileclient.global.ACTIVATION_CODE_KEY
import com.sensoguard.ccsmobileclient.global.IMEI_KEY
import com.sensoguard.ccsmobileclient.global.TEST_CODE
import com.sensoguard.ccsmobileclient.global.setStringInPreference


class ActivationActivity : ParentActivity() {

    private var myImei: String? = null
    private var tvImei: AppCompatTextView? = null
    private var btnShare: AppCompatButton? = null
    private var etEnterCode: AppCompatEditText? = null
    private var btnSignIn: AppCompatButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        init()

        myImei = intent.getStringExtra(IMEI_KEY)

        tvImei?.text = myImei
    }

    private fun init() {
        tvImei = findViewById(R.id.tvImei)
        btnShare = findViewById(R.id.btnShare)

        onClickShare()

        btnSignIn = findViewById(R.id.btnSignIn)

        onClickSignIn()

        etEnterCode = findViewById(R.id.etEnterCode)

        etEnterCode?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //hide the keyboard
                val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun onClickSignIn() {
        btnSignIn?.setOnClickListener {

            if (!validIsEmpty(etEnterCode)) {
                return@setOnClickListener
            }

            val myActivateCode = CryptoHandler.getInstance().encrypt(myImei)
            //if the activate code that came from user is equal to activate code that calculated by IMEI ,then start the app
            val enteredCode = etEnterCode?.text.toString()

            val myActivateCodeWhitespace = myActivateCode.filterNot { it.isWhitespace() }

            if (myActivateCodeWhitespace == enteredCode || enteredCode.startsWith(TEST_CODE)) {
                setStringInPreference(applicationContext, ACTIVATION_CODE_KEY, enteredCode)
                val inn = Intent(this, MainActivity::class.java)
                inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(inn)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.wrong_activate_code),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //check if the field of edit text is empty
    private fun validIsEmpty(editText: AppCompatEditText?): Boolean {
        if (editText?.text.isNullOrBlank()) {
            editText?.error = resources.getString(R.string.empty_field_error)
            return false
        }
        return true
    }


    private fun onClickShare() {
        btnShare?.setOnClickListener {
            //share the IMEI of the device
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.email_subject)
            )
            sharingIntent.putExtra(Intent.EXTRA_TEXT, myImei)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }
}
