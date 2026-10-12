package com.sensoguard.ccsmobileclient.global

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.opencsv.CSVWriter
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.Alarm
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar


private const val TAG = "SysMethodCsv"

//the folder in the cache of the app (shared by the FileProvider, see res/xml/file_path.xml)
private const val CSV_FOLDER_NAME = "csv"
private const val CSV_FILE_NAME = "SG-AlarmLog.csv"


private fun alarmTitlesToCsvRow(context: Context): Array<String> {
    val res = context.resources
    return arrayOf(
        res.getString(R.string.csv_date_title),
        res.getString(R.string.csv_name_title),
        res.getString(R.string.csv_id_title),
        res.getString(R.string.csv_alarm_type_title),
        res.getString(R.string.csv_latitude_title),
        res.getString(R.string.csv_longitude_title)
    )
}

private fun alarmToCsvRow(alarm: Alarm): Array<String> {
    return arrayOf(
        alarm.currentDate,
        alarm.name.toString(),
        alarm.id.toString(),
        alarm.type.toString(),
        alarm.latitude.toString(),
        alarm.longitude.toString()
    )
}

private fun getCsvFile(context: Context): File {
    val folder = File(context.cacheDir, CSV_FOLDER_NAME)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return File(folder, CSV_FILE_NAME)
}

/**
 * write the rows to the csv file
 * @return true if succeeded
 */
fun writeCsvFile(csvRows: List<Array<String>>, context: Context): Boolean {
    return try {
        CSVWriter(FileWriter(getCsvFile(context))).use { writer ->
            writer.writeAll(csvRows)
        }
        true
    } catch (e: IOException) {
        Log.e(TAG, "failed to write the csv file", e)
        false
    }
}


//Share the csv file
fun shareCsv(activity: Activity) {
    val sendIntent = Intent(Intent.ACTION_SEND)
    sendIntent.type = "text/csv"

    val resources = activity.resources
    val locale = resources.configuration.locales.getFirstMatch(resources.assets.locales)
    val dateFormat = SimpleDateFormat("dd/MM/yy", locale)
    val dateString = dateFormat.format(Calendar.getInstance().time)

    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard Alarm Log $dateString")
    val uri = FileProvider.getUriForFile(
        activity,
        activity.packageName, //the authority of the provider in the manifest
        getCsvFile(activity)
    )
    sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    activity.startActivity(Intent.createChooser(sendIntent, "Share file"))
}

/**
 * convert the alarms to rows of csv file (the first row is the titles)
 */
fun alarmsListToCsvFile(alarms: List<Alarm>?, context: Context): List<Array<String>> {
    val csvRows = ArrayList<Array<String>>()
    csvRows.add(alarmTitlesToCsvRow(context))
    alarms?.forEach { csvRows.add(alarmToCsvRow(it)) }
    return csvRows
}
