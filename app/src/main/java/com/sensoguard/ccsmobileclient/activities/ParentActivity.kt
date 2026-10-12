package com.sensoguard.ccsmobileclient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sensoguard.ccsmobileclient.global.configurationLanguage


open class ParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configurationLanguage(this)
        super.onCreate(savedInstanceState)
        //draw the screens edge to edge on all the versions of android (as on android 15 and above),
        //the layouts keep their content away from the system bars
        WindowCompat.enableEdgeToEdge(window)
        //the screens are light, so the icons of the system bars are dark
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

}
