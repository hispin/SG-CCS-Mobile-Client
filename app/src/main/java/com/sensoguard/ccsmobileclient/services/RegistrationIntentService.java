package com.sensoguard.ccsmobileclient.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.sensoguard.ccsmobileclient.classes.NotificationSettings;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.sensoguard.ccsmobileclient.global.ConstsKt.FCM_TOKEN_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.REGISTER_TOKEN_STATUS;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.REGISTRATION_ID_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.TOKEN_REGISTRATION_STATUS_KEY;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.getStringInPreference;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setBooleanInPreference;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setStringInPreference;

public class RegistrationIntentService extends IntentService {


    private static final String TAG = "RegIntentService";
    //SharedPreferences sharedPreferences;
    String resultString = null;
    String regID = null;
    String storedToken = null;
    String FCM_token = null;

    private NotificationHub hub;
    //private String myTag;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        resultString = null;
        regID = null;
        storedToken = null;

        try {
            //get the token from firebase
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    FCM_token = instanceIdResult.getToken();
                    Intent inn = new Intent("get.token.notification");
                    inn.putExtra("token", FCM_token);
                    sendBroadcast(inn);
                    Timber.d("FCM Registration Token: %s", FCM_token);
                }
            });

            //even if it has been failed to e
            TimeUnit.SECONDS.sleep(1);

            //check the status of token
            validationOption();

        } catch (Exception e) {
            Timber.e(e, resultString = "Failed to complete registration");
            setError();
            // If an exception happens while fetching the new token or updating registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

    }


    //check the status of token
    private void validationOption() {
        // Storing the registration ID that indicates whether the generated token has been
        // sent to your server. If it is not stored, send the token to your server.
        // Otherwise, your server should have already received the token.
        if (((regID = getStringInPreference(getApplicationContext(), REGISTRATION_ID_KEY, null)) == null)) {

            NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                    NotificationSettings.HubListenConnectionString, this);
            Timber.d("Attempting a new registration with NH using FCM token : %s", FCM_token);

            //register without tag
            try {
                regID = hub.register(FCM_token).getRegistrationId();
            } catch (Exception e) {
                e.printStackTrace();
                setError();
                return;
            }

            resultString = "New NH Registration Successfully - RegId : " + regID;
            Timber.d(resultString);

            setStringInPreference(getApplicationContext(), REGISTRATION_ID_KEY, regID);
            setStringInPreference(getApplicationContext(), FCM_TOKEN_KEY, FCM_token);
            //update the token registration status
            setBooleanInPreference(this, REGISTER_TOKEN_STATUS, true);
            sendBroadcast(new Intent(TOKEN_REGISTRATION_STATUS_KEY));
        }

        // Check to see if the token has been compromised and needs refreshing.
        else if (!(Objects.requireNonNull(storedToken = getStringInPreference(getApplicationContext(), FCM_TOKEN_KEY, ""))).equals(FCM_token)) {

            NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                    NotificationSettings.HubListenConnectionString, this);
            Timber.d("NH Registration refreshing with token : %s", FCM_token);

            //register without tag
            try {
                regID = hub.register(FCM_token).getRegistrationId();
            } catch (Exception e) {
                e.printStackTrace();
                setError();
                return;
            }

            // If you want to use tags...
            // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
            // regID = hub.register(token, "tag1,tag2").getRegistrationId();
            //regID = hub.register(FCM_token, myTag).getRegistrationId();

            resultString = "New NH Registration Successfully - RegId : " + regID;
            Timber.d(resultString);

            setStringInPreference(getApplicationContext(), REGISTRATION_ID_KEY, regID);
            setStringInPreference(getApplicationContext(), FCM_TOKEN_KEY, FCM_token);

            //update the token registration status
            setBooleanInPreference(this, REGISTER_TOKEN_STATUS, true);
            sendBroadcast(new Intent(TOKEN_REGISTRATION_STATUS_KEY));
        } else {
            resultString = "Previously Registered Successfully - RegId : " + regID;
            Timber.d(resultString);
            //update the token registration status
            setBooleanInPreference(this, REGISTER_TOKEN_STATUS, true);
            sendBroadcast(new Intent(TOKEN_REGISTRATION_STATUS_KEY));
        }

        Intent inn = new Intent("get.token.notification");
        inn.putExtra("token", FCM_token);
        sendBroadcast(inn);


        //showToast(resultString);
    }

    /**
     * set error
     */
    private void setError() {
        //update the token registration status
        setBooleanInPreference(this, REGISTER_TOKEN_STATUS, false);
        sendBroadcast(new Intent(TOKEN_REGISTRATION_STATUS_KEY));
        resultString = "Failed to complete registration";
        showToast(resultString);
    }


    /**
     * @param message
     */
    //show toast from other thread
    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }


}
