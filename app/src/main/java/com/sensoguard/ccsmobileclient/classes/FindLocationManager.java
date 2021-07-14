//package com.sensoguard.ccsmobileclient.classes;
//
//import android.Manifest;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import com.crowcloud.gatewaymobile.GatewayMobile;
//import com.crowcloud.gatewaymobile.R;
//import com.crowcloud.gatewaymobile.global.Consts;
//import com.crowcloud.gatewaymobile.global.GlobalMethods;
//import com.crowcloud.gatewaymobile.services.LocationReceiver;
//import com.google.gson.Gson;
//import com.intentfilter.androidpermissions.PermissionManager;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//
//public class FindLocationManager implements LocationListener {
//
//    private static final FindLocationManager ourInstance = new FindLocationManager();
//    private static String TAG = "FindLocationManager";
//    private static int locationRcvCount = 0;
//    protected LocationManager locationManager = null;
//    protected Criteria criteria;
//    private Context currentContext;
//
//    private FindLocationManager() {
//
//    }
//
//
//    public static FindLocationManager getInstance() {
//        return ourInstance;
//    }
//
//
//    public void getFineLocation(Context context) {
//        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        currentContext = context;
//
//        int currentApiVersion = Build.VERSION.SDK_INT;
//        if (currentApiVersion >= Build.VERSION_CODES.M && !GlobalMethods.checkLocationPermission(context)) {
//            requestLocationPermission(context);
//        } else {
//            Log.d("testLocation", "request location");
//
//            String mobileName = GlobalMethods.getDeviceName();
//            //requestSingleUpdate does not work for LGE
//            if (mobileName.substring(0, 3).equalsIgnoreCase("LGE")) {
//                Log.d("testLocation", "lg request location");
//                try {
//                    locationRcvCount = 0;
//                    locationManager.requestLocationUpdates(0, 0, criteria, FindLocationManager.this, null);//.requestSingleUpdate(criteria, this, null);
//
//                } catch (SecurityException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.d("testLocation", "no lg request location");
//                Intent intent = new Intent(context, LocationReceiver.class);
//                PendingIntent pendingIntent = PendingIntent
//                        .getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                try {
//                    locationManager.requestSingleUpdate(criteria, pendingIntent);
//                } catch (IllegalArgumentException | SecurityException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    public void requestLocationPermission(final Context context) {
//
//        PermissionManager permissionManager = PermissionManager.getInstance(context);
//        Collection<String> permissions = Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissionManager.checkPermissions(permissions, new PermissionManager.PermissionRequestListener() {
//            @Override
//            public void onPermissionGranted() {
//                int currentApiVersion = Build.VERSION.SDK_INT;
//                if (currentApiVersion >= Build.VERSION_CODES.M && !GlobalMethods.checkLocationPermission(context)) {
//                    GlobalMethods.showShortTost(context, R.string.permission_denied);
//                } else {
//                    Intent intent = new Intent(context, LocationReceiver.class);
//                    PendingIntent pendingIntent = PendingIntent
//                            .getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    locationManager.requestSingleUpdate(criteria, pendingIntent);
//                }
//            }
//
//            @Override
//            public void onPermissionDenied() {
//                GlobalMethods.showShortTost(context, R.string.permission_denied);
//            }
//        });
//    }
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//        if (locationRcvCount > Consts.MAX_RCV_LOCATION) {
//            locationRcvCount = 0;
//            locationManager.removeUpdates(this);
//        } else {
//            locationRcvCount++;
//        }
//        Log.d("testLocation", "not accurate");
//        if (currentContext != null && location.hasAccuracy() && location.getAccuracy() <= Consts.MIN_RANGE_LOCATION_ACCURATE) {
//            sendLocation(location);
//            Log.d("testLocation", "accurate");
//            locationRcvCount = 0;
//            locationManager.removeUpdates(this);
//
//            Gson js = new Gson();
//            String extras = js.toJson(location);
//            saveLastDateOfGetLocation(extras, currentContext);
//            saveLastProvider(extras, currentContext);
//        }
//    }
//
//    private void sendLocation(Location location) {
//        final Gson gson = new Gson();
//        String extras = gson.toJson(location);
//        Intent inn = new Intent(GatewayMobile.currentPackage + ".get.current.location");
//        inn.putExtra("currentLocation", extras);
//        currentContext.sendBroadcast(inn);
//    }
//
//    private void saveLastProvider(String extras, Context context) {
//        try {
//            JSONObject js = new JSONObject(extras);
//            if (js.has("mProvider")) {
//                String lastProvider = js.getString("mProvider");
//                GlobalMethods.setStringInPreference(context, Consts.LAST_PROVIDER_LOCATION_PREF, lastProvider);
//                Log.d(TAG, lastProvider + "");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void saveLastDateOfGetLocation(String extras, Context context) {
//        try {
//            JSONObject js = new JSONObject(extras);
//            if (js.has("mTime")) {
//                long dateLastLocation = js.getLong("mTime");
//                GlobalMethods.setLongInPreference(context, Consts.LAST_DATE_LOCATION_PREF, dateLastLocation);
//                Log.d(TAG, dateLastLocation + "");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.d("testLocation", "onStatusChanged");
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        Log.d("testLocation", "onProviderEnabled");
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Log.d("testLocation", "onProviderDisabled");
//    }
//
//
//}
