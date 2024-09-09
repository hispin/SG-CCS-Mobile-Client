package com.sensoguard.ccsmobileclient.services;

import static com.sensoguard.ccsmobileclient.global.ConstsKt.FCM_TOKEN_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.REGISTER_TOKEN_STATUS;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.REGISTRATION_ID_KEY;
import static com.sensoguard.ccsmobileclient.global.ConstsKt.TOKEN_REGISTRATION_STATUS_KEY;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setBooleanInPreference;
import static com.sensoguard.ccsmobileclient.global.SysMethodsSharedPrefKt.setStringInPreference;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import timber.log.Timber;

public class RegistrationIntentService extends IntentService {


    private static final String TAG = "RegIntentService";
    //SharedPreferences sharedPreferences;
    String resultString = null;
    String regID = null;
    String storedToken = null;
    String FCM_token = null;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        resultString = null;
        regID = null;
        storedToken = null;

        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FCM_token = task.getResult();
                            Log.d("testToken", "FCM_token " + FCM_token);
                            setStringInPreference(getApplicationContext(), REGISTRATION_ID_KEY, regID);
                            setStringInPreference(getApplicationContext(), FCM_TOKEN_KEY, FCM_token);
                            //update the token registration status
                            setBooleanInPreference(this, REGISTER_TOKEN_STATUS, true);
                            //sendBroadcast(new Intent(TOKEN_REGISTRATION_STATUS_KEY));
                        }
                    });
        } catch (Exception e) {
            Timber.e(e, resultString = "Failed to complete registration");
            setError();
            // If an exception happens while fetching the new token or updating registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }
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
