package com.sensoguard.ccsmobileclient.classes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * offer to send the crash details by email, then let the default handler finish the crash
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "MyExceptionHandler";

    private final Activity activity;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public MyExceptionHandler(Activity a) {
        activity = a;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        String errorMsg = Log.getStackTraceString(ex);
        Log.e(TAG, errorMsg);
        sendEmail(errorMsg);
        activity.finish();

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        } else {
            System.exit(2);
        }
    }

    private void sendEmail(String msg) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hag.swead@gmail.com", "tomer@sensoguard.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try {
            activity.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "no email app", e);
        }
    }
}
