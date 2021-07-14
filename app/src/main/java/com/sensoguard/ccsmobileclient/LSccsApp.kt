package com.sensoguard.ccsmobileclient

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu
import com.sensoguard.ccsmobileclient.classes.LanguageManager
import com.sensoguard.ccsmobileclient.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.ccsmobileclient.global.getAppLanguage
import com.sensoguard.ccsmobileclient.global.getStringInPreference
import com.sensoguard.ccsmobileclient.global.setAppLanguage
import java.util.*

class LSccsApp : Application() {

//   override fun onConfigurationChanged(newConfig: Configuration) {
////        if (newConfig != null) {
////            super.onConfigurationChanged(newConfig)
////        }
////        setLocale()
//    }

    private fun setLocale() {
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        val locale: Locale = getLocale(this)

        var mCurrentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            configuration.locale
        }

        if (mCurrentLocale != locale) {
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, null)
        }
    }

    private fun getLocale(context: Context?): Locale {
        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lang = getStringInPreference(context, CURRENT_LANG_KEY_PREF, "en")
        //var lang = sharedPreferences.getString("language", "en")

        return Locale(lang)
    }

    private fun configurationLanguage() {
        LanguageManager.setLanguageList()
        val currentLanguage = getStringInPreference(this, CURRENT_LANG_KEY_PREF, "-1")
        if (currentLanguage != "-1") {
            GeneralItemMenu.selectedItem = currentLanguage
            setAppLanguage(this, GeneralItemMenu.selectedItem)
        } else {
            val deviceLang = getAppLanguage()
            if (LanguageManager.isExistLang(deviceLang)) {
                GeneralItemMenu.selectedItem = deviceLang
                setAppLanguage(this, GeneralItemMenu.selectedItem)
            }
        }

    }
}