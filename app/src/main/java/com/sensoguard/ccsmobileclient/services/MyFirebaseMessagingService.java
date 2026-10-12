package com.sensoguard.ccsmobileclient.services;

import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_CAR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_CAR_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_DIGGING_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_DISCONNECTED;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_DISCONNECTED_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_DUAL_TECH;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_DUAL_TECH_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_EXTERNAL_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_FOOTSTEPS_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_GATEWAY_DISCONNECTED;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_GATEWAY_DISCONNECTED_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_INTRUDER;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_KEEP_ALIVE;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_KEEP_ALIVE_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_LOW_BATTERY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_LOW_BATTERY_STR;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_MOTION;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_SENSOR_OFF;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.ALARM_TYPE_INDEX_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_IS_ARMED;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.CREATE_ALARM_TYPE_KEY;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.addAlarmToQueue;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.populateAlarmsFromLocally;
import static com.sensoguard.ccsmobileclient.global.SysAlarmsManagerKt.storeAlarmsToLocally;
import static com.sensoguard.ccsmobileclient.global.SysFcmKt.storeFcmToken;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sensoguard.ccsmobileclient.activities.MainActivity;
import com.sensoguard.ccsmobileclient.classes.Alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "nh-demo-channel-id";
    public static final String NOTIFICATION_CHANNEL_NAME = "Notification Hubs Demo Channel";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification Hubs Demo Channel";
    private static final String TAG = "FirebaseService";

    public static void createChannelAndHandleNotifications(Context context) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
        channel.setShowBadge(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    //the token is still delivered by this callback (see registerFcmToken)
    @SuppressWarnings("deprecation")
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //keep the current token (it can be shared from the configuration screen)
        storeFcmToken(this, token);
    }

    @Override
    public void onRegistered(@NonNull String token) {
        super.onRegistered(token);
        storeFcmToken(this, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "accept msg");
        createChannelAndHandleNotifications(getApplicationContext());
        //play the sound only when an alarm has been accepted
        if (parseIntentExtra(remoteMessage.toIntent())) {
            startWorkerMedia();
        }
    }

    private void startWorkerMedia() {
        OneTimeWorkRequest mediaWorkRequest = new OneTimeWorkRequest.Builder(MediaWorker.class).build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(MediaWorker.UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, mediaWorkRequest);
    }

    /**
     * get the index of the alarm type (used for languages other than english)
     *
     * @param typeAlarm the alarm type as received from the server
     * @return the index of the type, 0 if unknown
     */
    private static int getAlarmTypeIndex(String typeAlarm) {
        switch (typeAlarm) {
            case ALARM_CAR_STR:
                return ALARM_CAR;
            case ALARM_FOOTSTEPS_STR:
                return ALARM_INTRUDER;
            case ALARM_DIGGING_STR:
                return ALARM_MOTION;
            case ALARM_EXTERNAL_STR:
                return ALARM_SENSOR_OFF;
            case ALARM_DISCONNECTED_STR:
                return ALARM_DISCONNECTED;
            case ALARM_KEEP_ALIVE_STR:
                return ALARM_KEEP_ALIVE;
            case ALARM_LOW_BATTERY_STR:
                return ALARM_LOW_BATTERY;
            case ALARM_DUAL_TECH_STR:
                return ALARM_DUAL_TECH;
            case ALARM_GATEWAY_DISCONNECTED_STR:
                return ALARM_GATEWAY_DISCONNECTED;
            default:
                return 0;
        }
    }

    /**
     * parse intent extra came from server
     *
     * @param inn the intent of the message
     * @return true if the message contains a valid alarm
     */
    private boolean parseIntentExtra(Intent inn) {
        Bundle extras = inn.getExtras();
        if (extras == null) {
            Log.w(TAG, "message without extras");
            return false;
        }

        String message = extras.getString("message", "-1");

        if (message.equals("-1")) {
            //new system: the alarm details are sent as separated fields
            try {
                double lat = Double.parseDouble(extras.getString("latitude", ""));
                double lon = Double.parseDouble(extras.getString("longitude", ""));
                String alarmId = extras.getString("gateway", "");
                String typeAlarm = extras.getString("alarmType", "");
                String zone = extras.getString("unit", "");
                int typeIdx = getAlarmTypeIndex(typeAlarm);

                sendingManage(alarmId, lat, lon, typeAlarm, typeIdx, zone, true);
                sendNotification(alarmId, typeAlarm, zone);
                return true;
            } catch (NumberFormatException ex) {
                Log.e(TAG, "invalid location in message", ex);
            }
        } else {
            //old system: "<alarmId> <lat>,<lon> (<type>)"
            String[] arr = message.split(" ");
            if (arr.length > 2) {
                String coordinates = arr[1];
                String[] tmp = coordinates.split(",");
                if (tmp.length > 1) {
                    try {
                        double lat = Double.parseDouble(tmp[0]);
                        double lon = Double.parseDouble(tmp[1]);

                        String alarmId = arr[0];
                        String[] tmpArr = alarmId.split("-");
                        String zone = "";
                        if (tmpArr.length > 1) {
                            zone = tmpArr[1];
                        }

                        //remove Parenthesis
                        String typeAlarm = arr[2].replace("(", "").replace(")", "");
                        int typeIdx = getAlarmTypeIndex(typeAlarm);

                        sendingManage(alarmId, lat, lon, typeAlarm, typeIdx, zone, false);
                        sendNotification(alarmId, typeAlarm, zone);
                        return true;
                    } catch (NumberFormatException ex) {
                        Log.e(TAG, "invalid location in message", ex);
                    }
                }
            }
        }
        return false;
    }

    /**
     * send notification
     *
     * @param alarmId   the id of the alarm
     * @param typeAlarm the type of the alarm
     * @param zone      the zone of the alarm
     */
    private void sendNotification(String alarmId, String typeAlarm, String zone) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String showMsg = alarmId + "," + zone + "," + typeAlarm;

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        long oneTimeID = SystemClock.uptimeMillis();

        //set different request code to make different extra for each notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) oneTimeID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID)
                .setContentText(showMsg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //remove after click on notification
                .setAutoCancel(true);

        notificationBuilder.setContentIntent(contentIntent);

        //send
        notificationManager.notify((int) oneTimeID, notificationBuilder.build());
    }

    /**
     * add the alarm to the queue and to the history, and notify the screens
     *
     * @param alarmId     the id of the alarm
     * @param lat         the latitude of the alarm
     * @param lon         the longitude of the alarm
     * @param typeAlarm   the type of the alarm
     * @param typeIdx     the index of the alarm type
     * @param zone        the zone of the alarm
     * @param isNewSystem true if the alarm came from the new system
     */
    private void sendingManage(String alarmId, double lat, double lon, String typeAlarm, int typeIdx, String zone, boolean isNewSystem) {
        addAlarmToQueue(alarmId, lat, lon, typeAlarm, typeIdx, zone);

        //add alarm to history
        addAlarmToHistory(alarmId, typeAlarm, lat, lon, zone, isNewSystem);

        sendAlarm(alarmId, typeAlarm);
    }

    /**
     * notify the screens about new alarm
     *
     * @param alarmId   the id of the alarm
     * @param typeAlarm the type of the alarm
     */
    private void sendAlarm(String alarmId, String typeAlarm) {
        Intent inn = new Intent(CREATE_ALARM_KEY);
        inn.putExtra(CREATE_ALARM_ID_KEY, alarmId);
        inn.putExtra(CREATE_ALARM_NAME_KEY, alarmId);
        inn.putExtra(CREATE_ALARM_IS_ARMED, true);
        inn.putExtra(CREATE_ALARM_TYPE_KEY, typeAlarm);
        inn.putExtra(ALARM_TYPE_INDEX_KEY, -1);
        sendBroadcast(inn.setPackage(getPackageName()));
    }

    /**
     * add alarm to history
     *
     * @param alarmId     the id of the alarm
     * @param typeAlarm   the type of the alarm
     * @param lat         the latitude of the alarm
     * @param lon         the longitude of the alarm
     * @param zone        the zone of the alarm
     * @param isNewSystem true if the alarm came from the new system
     */
    private void addAlarmToHistory(String alarmId, String typeAlarm, double lat, double lon, String zone, boolean isNewSystem) {
        Calendar tmp = Calendar.getInstance();
        Resources resources = this.getResources();
        Locale locale = resources.getConfiguration().getLocales().getFirstMatch(resources.getAssets().getLocales());

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
            storeAlarmsToLocally(alarms, this);
        }
    }
}
