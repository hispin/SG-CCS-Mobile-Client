package com.sensoguard.ccsmobileclient.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sensoguard.ccsmobileclient.activities.MainActivity;
import com.sensoguard.ccsmobileclient.classes.Alarm;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_TYPE_INDEX_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_IS_ARMED;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_TYPE_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.LAST_LATITUDE;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.LAST_LONGETITUDE;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.addAlarmToQueue;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.populateAlarmsFromLocally;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.storeAlarmsToLocally;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setDoubleInPreference;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "nh-demo-channel-id";
    public static final String NOTIFICATION_CHANNEL_NAME = "Notification Hubs Demo Channel";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification Hubs Demo Channel";
    public static final int NOTIFICATION_ID = 1;
    static Context ctx;
    private final String TAG = "FirebaseService";
    NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;

    public static void createChannelAndHandleNotifications(Context context) {
        ctx = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //String  nhMessage = remoteMessage.getData().values().iterator().next();
        Timber.d("accept msg");
        Intent myIntent = remoteMessage.toIntent();
        if (myIntent != null) {

//                myIntent.setAction("show.body.notification");
//                sendBroadcast(myIntent);

            parseIntentExtra(myIntent);
            sendNotification(myIntent);
        }
    }

    //parse intent extra came from server
    private void parseIntentExtra(Intent inn) {
        //try{

        String title = inn.getExtras().getString("title");
        String message = inn.getExtras().getString("message");
        String link = inn.getExtras().getString("openURL");
        String imagePath = inn.getExtras().getString("image");

        ///test from firebase
        message = inn.getExtras().getString("gcm.notification.body");


        if (title != null)
            Timber.d(title);
        if (message != null)
            Timber.d(message);
        if (link != null)
            Timber.d(link);
        if (imagePath != null)
            Timber.d(imagePath);


        if (message != null) {
            String[] arr = message.split(" ");
            if (arr.length > 2) {
                String coordinates = arr[1];
                String[] tmp = coordinates.split(",");
                if (tmp.length > 1) {
                    String latitude = tmp[0];
                    String longtitude = tmp[1];
                    try {
                        double lat = Double.parseDouble(latitude);
                        double lon = Double.parseDouble(longtitude);
                        Timber.d("lat=" + lat + " lon=" + lon);

                        //save locally for default location
                        setDoubleInPreference(ctx, LAST_LATITUDE, lat);
                        setDoubleInPreference(ctx, LAST_LONGETITUDE, lon);

                        String alarmId = arr[0];

                        //remove Parenthesis
                        String typeAlarm = arr[2].replace("(",
                                "");
                        typeAlarm = typeAlarm.replace(")",
                                "");

                        //save the index of type for other languages then english
                        int typeIdx = 0;
                        if (typeAlarm.equals("Car"))
                            typeIdx = 0;
                        else if (typeAlarm.equals("Intruder"))
                            typeIdx = 1;

                        sendingManage(alarmId, lat, lon, typeAlarm, true, typeIdx);
//                        addAlarmToQueue(alarmId,lat,lon,typeAlarm,true,typeIdx);
//
//                        //add alarm to history
//                        addAlarmToHistory(alarmId,typeAlarm,lat,lon);
//
//                        sendAlarm(alarmId,typeAlarm,lat,lon);


                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }


        //tvTo?.text = to
//            tvTitle?.text = title
//            tvMsg?.text = message
//            //tvOpenUrl?.text = link
//            link?.let { tvOpenUrl?.htmlText(it) }
//            imagePath?.let { ibShowPic?.let { it1 -> showPicture(it, it1) } }
//            Log.d("","")
//        } catch (JSONException e) {
//
//        }
    }

    /**
     * @param alarmId
     * @param lat
     * @param lon
     * @param typeAlarm
     * @param isArmed
     * @param typeIdx
     */
    private void sendingManage(String alarmId, double lat, double lon, String typeAlarm, boolean isArmed, int typeIdx) {
        addAlarmToQueue(alarmId, lat, lon, typeAlarm, isArmed, typeIdx);

        //add alarm to history
        addAlarmToHistory(alarmId, typeAlarm, lat, lon);

        sendAlarm(alarmId, typeAlarm, lat, lon);
    }

    /**
     * add alarm to history
     *
     * @param alarmId
     * @param typeAlarm
     * @param lat
     * @param lon
     */
    private void addAlarmToHistory(String alarmId, String typeAlarm, double lat, double lon) {
        Calendar tmp = Calendar.getInstance();
        Resources resources = this.getResources();
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            locale = resources.getConfiguration().getLocales().getFirstMatch(resources.getAssets().getLocales());

        else
            locale = resources.getConfiguration().locale;

        SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss dd/MM/yy", locale);
        String dateString = dateFormat.format(tmp.getTime());

        Alarm alarm = new Alarm(
                alarmId,
                alarmId,
                typeAlarm,
                dateString,
                true,
                tmp.getTimeInMillis()
        );
        alarm.setLatitude(lat);
        alarm.setLongitude(lon);

        alarm.setLocallyDefined(true);

        //extract the current locally
        ArrayList<Alarm> alarms = populateAlarmsFromLocally(this);
        if (alarms != null) {
            alarms.add(alarm);
        }
        if (alarms != null) {
            storeAlarmsToLocally(alarms, this);
        }
    }

    /**
     * @param alarmId
     * @param typeAlarm
     * @param lat
     * @param lon
     */
    private void sendAlarm(String alarmId, String typeAlarm, double lat, double lon) {
        Intent inn = new Intent(CREATE_ALARM_KEY);
        inn.putExtra(CREATE_ALARM_ID_KEY, alarmId);
        inn.putExtra(CREATE_ALARM_NAME_KEY, alarmId);
        inn.putExtra(CREATE_ALARM_IS_ARMED, true);
        inn.putExtra(CREATE_ALARM_TYPE_KEY, typeAlarm);
        inn.putExtra(ALARM_TYPE_INDEX_KEY, -1);
        sendBroadcast(inn);
    }

    private void sendNotification(Intent myIntent) {

        if (myIntent == null)
            return;

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = Objects.requireNonNull(myIntent.getExtras()).getString("title");
        String message = Objects.requireNonNull(myIntent.getExtras()).getString("message");

        if (title != null)
            Log.d("CSSmyFire", title);

        //add extras data that accepted from push
        intent.putExtras(myIntent);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);


        long oneTimeID = SystemClock.uptimeMillis();

        //set different request code to make different extra for each notification
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //remove after click on notification
                .setAutoCancel(true);

        notificationBuilder.setContentIntent(contentIntent);

        //send
        mNotificationManager.notify((int) oneTimeID, notificationBuilder.build());

    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);


        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long oneTimeID = SystemClock.uptimeMillis();
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL_ID)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //remove after click on notification
                .setAutoCancel(true);

        notificationBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify((int) oneTimeID, notificationBuilder.build());
        //mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
    }
}
