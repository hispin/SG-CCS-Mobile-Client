package com.sensoguard.ccsmobileclient.services;

import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_TYPE_INDEX_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_IS_ARMED;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_TYPE_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.LAST_LATITUDE;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.LAST_LONGETITUDE;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.TEST_EVENT_MSG_KEY;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.addAlarmToQueue;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.populateAlarmsFromLocally;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.storeAlarmsToLocally;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setDoubleInPreference;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sensoguard.ccsmobileclient.activities.MainActivity;
import com.sensoguard.ccsmobileclient.classes.Alarm;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "nh-demo-channel-id";
    public static final String NOTIFICATION_CHANNEL_NAME = "Notification Hubs Demo Channel";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification Hubs Demo Channel";
    public static final int NOTIFICATION_ID = 1;
    static Context ctx;
    private final String TAG = "FirebaseService";
    NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), TEST_EVENT_MSG_KEY)) {
                //val commandType = intent.getStringExtra(COMMAND_TYPE)
                double lat = intent.getDoubleExtra("Latitude", -1);
                double lon = intent.getDoubleExtra("Longitude", -1);
                String alarmId = intent.getStringExtra("AlarmID");
                String typeAlarm = intent.getStringExtra("AlarmType");

                //set zone (zone)
                String[] tmp1 = alarmId.split("-");
                String zone = "";
                if (tmp1 != null && tmp1.length > 1) {
                    zone = tmp1[1];
                }
                sendNotification("test");
                sendingManage(alarmId, lat, lon, typeAlarm, true, 0, zone, false);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("testTest", "onCreate server");
        setFilter();
    }

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
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (java.lang.Exception ex) {
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("testAlarmMap", "accept msg");
        Intent myIntent = remoteMessage.toIntent();
        if (myIntent != null) {
            parseIntentExtra(myIntent);
            createChannelAndHandleNotifications(getApplicationContext());
            sendNotification(myIntent);
            //start media
            //startServiceMedia();
            startWorkerMedia();
        }
    }

    private void startWorkerMedia() {
        OneTimeWorkRequest mediaWorkRequest = new OneTimeWorkRequest.Builder(MediaWorker.class).build();//OneTimeWorkRequestBuilder < MediaWorker > ().build();
        WorkManager.getInstance(ctx).enqueue(mediaWorkRequest);
    }

    /**
     * check if the string is format of json
     *
     * @param json
     * @return
     */
    public boolean isJsonValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    //parse intent extra came from server
    private void parseIntentExtra(Intent inn) {

        String title = inn.getExtras().getString("title");
        String message = inn.getExtras().getString("message");
        String link = inn.getExtras().getString("openURL");
        String imagePath = inn.getExtras().getString("image");


        if (title != null)
            Timber.d(title);
        if (message != null)
            Timber.d(message);
        if (link != null)
            Timber.d(link);
        if (imagePath != null)
            Timber.d(imagePath);

        if (isJsonValid(message)) {
            try {
                JSONObject jObject = new JSONObject(message);
                double lat = jObject.getDouble("Latitude");
                double lon = jObject.getDouble("Longitude");
                //Log.d("testAlarmMap","lat "+lat+" lon "+lon);
                String alarmId = jObject.getString("Gateway");
                String typeAlarm = jObject.getString("AlarmType");
                String zone = jObject.getString("Unit");
                //save the index of type for other languages then english
                int typeIdx = 0;
                if (typeAlarm.equals("Car"))
                    typeIdx = 0;
                else if (typeAlarm.equals("Footsteps"))
                    typeIdx = 1;
                sendingManage(alarmId, lat, lon, typeAlarm, true, typeIdx, zone, true);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
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
                            //Log.d("testAlarmMap","lat "+lat+" lon "+lon);
                            Timber.d("lat=" + lat + " lon=" + lon);

                            //save locally for default location
                            setDoubleInPreference(ctx, LAST_LATITUDE, lat);
                            setDoubleInPreference(ctx, LAST_LONGETITUDE, lon);

                            String alarmId = arr[0];
                            String[] tmpArr = alarmId.split("-");
                            String zone = "";
                            if (tmpArr != null && tmpArr.length > 1) {
                                zone = tmpArr[1];
                            }

                            //remove Parenthesis
                            String typeAlarm = arr[2].replace("(",
                                    "");
                            typeAlarm = typeAlarm.replace(")",
                                    "");

                            //save the index of type for other languages then english
                            int typeIdx = 0;
                            if (typeAlarm.equals("Car"))
                                typeIdx = 0;
                            else if (typeAlarm.equals("Footsteps"))
                                typeIdx = 1;

                            sendingManage(alarmId, lat, lon, typeAlarm, true, typeIdx, zone, false);

                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    /**
     * @param alarmId
     * @param lat
     * @param lon
     * @param typeAlarm
     * @param isArmed
     * @param typeIdx
     * @param zone
     */
    private void sendingManage(String alarmId, double lat, double lon, String typeAlarm, boolean isArmed, int typeIdx, String zone, Boolean isNewSystem) {
        addAlarmToQueue(alarmId, lat, lon, typeAlarm, isArmed, typeIdx);

        //add alarm to history
        addAlarmToHistory(alarmId, typeAlarm, lat, lon, zone, isNewSystem);


        sendAlarm(alarmId, typeAlarm, lat, lon);
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

    /**
     * add alarm to history
     *
     * @param alarmId
     * @param typeAlarm
     * @param lat
     * @param lon
     * @param zone
     * @param isNewSystem
     */
    private void addAlarmToHistory(String alarmId, String typeAlarm, double lat, double lon, String zone, Boolean isNewSystem) {
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
                tmp.getTimeInMillis(),
                zone,
                isNewSystem
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

    private void sendNotification(Intent myIntent) {

        if (myIntent == null)
            return;

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = Objects.requireNonNull(myIntent.getExtras()).getString("title");
        String message = Objects.requireNonNull(myIntent.getExtras()).getString("message");
        String showMsg = "";
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(message);
            String alarmId = jObject.getString("Gateway");
            showMsg += alarmId;
            String zone = jObject.getString("Unit");
            showMsg += "," + zone;
            String typeAlarm = jObject.getString("AlarmType");
            showMsg += "," + typeAlarm;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


//        if (title != null)
//            Log.d("CSSmyFire", title);

        //add extras data that accepted from push
        intent.putExtras(myIntent);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);


        long oneTimeID = SystemClock.uptimeMillis();

        PendingIntent contentIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

//        NotificationCompat.BigTextStyle bigStyle =
//                new NotificationCompat.BigTextStyle();
//        bigStyle.setBigContentTitle(title);
//        bigStyle.bigText(showMsg);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL_ID)
                //.setStyle(bigStyle)
                .setContentTitle(title)
                .setContentText(showMsg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //remove after click on notification
                .setAutoCancel(true);

        notificationBuilder.setContentIntent(contentIntent);

        //send
        mNotificationManager.notify((int) oneTimeID, notificationBuilder.build());

    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        long oneTimeID = SystemClock.uptimeMillis();
        PendingIntent contentIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


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

    private void setFilter() {
        IntentFilter filter = new IntentFilter(TEST_EVENT_MSG_KEY);
        registerReceiver(receiver, filter);
    }

    //start service media
    private void startServiceMedia() {
        Intent serviceIntent = new Intent(this, MediaService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

}
