package com.sensoguard.detectsensor.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.crashlytics.android.Crashlytics
import com.sensoguard.detectsensor.classes.GeneralItemMenu
import com.sensoguard.detectsensor.classes.LanguageManager
import com.sensoguard.detectsensor.classes.MyExceptionHandler
import com.sensoguard.detectsensor.global.*
import io.fabric.sdk.android.Fabric



class MainActivity : AppCompatActivity() {

    private var clickConsSensorTable:ConstraintLayout?=null
    private var clickConsMap:ConstraintLayout?=null
    private var clickConsConfiguration:ConstraintLayout?=null
    private var clickAlarmLog:ConstraintLayout?=null

//    @Override
//    protected override fun attachBaseContext(newBase:Context) {
//        configurationLanguage()
//    }



    override fun onCreate(savedInstanceState: Bundle?) {

        configureGeneralCatch()
        Fabric.with(this, Crashlytics())

        super.onCreate(savedInstanceState)

        configurationLanguage()

        setContentView(com.sensoguard.detectsensor.R.layout.activity_main)


        //hide unwanted badge of app icon
        hideBudgetNotification()

        initViews()
        setOnClickSensorTable()
        setOnClickMapTable()
        setOnClickConfigTable()
        setOnClickAlarmLogTable()

        //hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    //hide unwanted badge of app icon
    private fun hideBudgetNotification() {
        val id = "my_channel_01"
        val name = getString(com.sensoguard.detectsensor.R.string.channel_name)
        val descriptionText = getString(com.sensoguard.detectsensor.R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel =
            NotificationChannel(id, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        } else {

        }

    }

    private fun configureGeneralCatch() {
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(MyScreensActivity@ this))
    }

    private fun setOnClickSensorTable() {
        clickConsSensorTable?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,0)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickMapTable() {
        clickConsMap?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,1)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickConfigTable() {
        clickConsConfiguration?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,2)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickAlarmLogTable() {
        clickAlarmLog?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,3)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }

    private fun initViews() {
        clickConsSensorTable=findViewById(com.sensoguard.detectsensor.R.id.clickConsSensorTable)
        clickConsMap=findViewById(com.sensoguard.detectsensor.R.id.clickConsMap)
        clickConsConfiguration=findViewById(com.sensoguard.detectsensor.R.id.clickConsConfiguration)
        clickAlarmLog=findViewById(com.sensoguard.detectsensor.R.id.clickAlarmLog)
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
