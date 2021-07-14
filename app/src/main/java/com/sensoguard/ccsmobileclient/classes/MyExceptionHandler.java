package com.sensoguard.ccsmobileclient.classes;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public MyExceptionHandler(Activity a) {
        activity = a;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        String errorMsg = printLog(ex);
        sendEmail(errorMsg);
        activity.finish();
        System.exit(2);


//        if (BuildConfig.REPORT_CRASH) {
//
//            FirebaseCrash.report(ex);
//
//            restartApp();
//        } else if (ex.getStackTrace() != null) {
//
//            sendEmail(errorMsg);
//            activity.finish();
//            System.exit(2);
//        }
        //throw new RuntimeException("This is a crash");
    }

    private void sendEmail(String msg) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hag.swead@gmail.com","tomer@sensoguard.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try {
            activity.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }

    }

//    private void restartApp() {
//        Intent intent = new Intent(activity, MyScreensActivity.class);
//        intent.putExtra("crash", true);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK
//                | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(GatewayMobile.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager mgr = (AlarmManager) GatewayMobile.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
//        activity.finish();
//        System.exit(2);
//    }

    private String printLog(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(new PrintWriter(sw));
        if (pw != null) {
            Log.e("MyExceptionHandler", Log.getStackTraceString(ex));
        }
        return Log.getStackTraceString(ex);
    }
}

