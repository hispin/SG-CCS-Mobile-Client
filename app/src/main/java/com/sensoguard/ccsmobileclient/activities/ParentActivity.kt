package com.sensoguard.ccsmobileclient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sensoguard.ccsmobileclient.global.configurationLanguage


open class ParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configurationLanguage(this)
        super.onCreate(savedInstanceState)
    }

}
