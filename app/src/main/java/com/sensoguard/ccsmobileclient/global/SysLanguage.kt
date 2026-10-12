package com.sensoguard.ccsmobileclient.global

import android.content.Context
import android.content.res.Configuration
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu
import com.sensoguard.ccsmobileclient.classes.LanguageManager
import java.util.Locale


//Get the current language of the app
fun getAppLanguage(): String {
    return Locale.getDefault().language
}

fun configurationLanguage(context: Context) {
    LanguageManager.setLanguageList()
    val currentLanguage = getStringInPreference(context, CURRENT_LANG_KEY_PREF, "-1")
    if (currentLanguage != null && currentLanguage != "-1") {
        GeneralItemMenu.selectedItem = currentLanguage
        setAppLanguage(context, GeneralItemMenu.selectedItem)
    } else {
        val deviceLang = getAppLanguage()
        if (LanguageManager.isExistLang(deviceLang)) {
            GeneralItemMenu.selectedItem = deviceLang
            setAppLanguage(context, GeneralItemMenu.selectedItem)
        }
    }
}

//set language for the application
fun setAppLanguage(c: Context, lang: String) {
    val localeNew = Locale.forLanguageTag(lang)
    Locale.setDefault(localeNew)

    val res = c.resources
    val newConfig = Configuration(res.configuration)
    newConfig.setLocale(localeNew)
    newConfig.setLayoutDirection(localeNew)

    //the language is changed in place for the activities and the services of the app
    @Suppress("DEPRECATION")
    res.updateConfiguration(newConfig, res.displayMetrics)
}
