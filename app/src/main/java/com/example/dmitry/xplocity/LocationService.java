package com.example.dmitry.xplocity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

public class LocationService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private boolean tracking_active = false;

    public ArrayList<LatLng> route;
    public LatLng last_position;

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {

            if (tracking_active) {
                last_position = new LatLng(location.getLatitude(), location.getLongitude());
                route.add(last_position);

                write_state_to_storage();
                Log.e("XPLOCITY SERVICE", "location_changed");
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            //new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        Log.e("Xplocity service", "Service started");

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Xplocity")
                .setContentText("Tracking started")
                .build();

        startForeground(1338, notification);


        // load tasks from preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (prefs.getBoolean("tracking_active", false)) {
            tracking_active = true;

            Gson gson = new Gson();
            route = gson.fromJson(prefs.getString("route", ""), new TypeToken<ArrayList<LatLng>>() {
            }.getType());

            prefs.edit().remove("route");
            prefs.edit().remove("tracking_active");

            startTracking();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        scheduleService();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        scheduleService();
    }


    private void scheduleService() {

        write_state_to_storage();

        Intent myIntent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.e("Xplocity service", "Service stopped");
    }

    private void write_state_to_storage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(route);
        editor.putString("route", json);
        editor.putBoolean("tracking_active", tracking_active);


        editor.commit();
    }


    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }


    }


    public void startTracking() {

        tracking_active = true;

        if (route == null) {
            route = new ArrayList<LatLng>();
        } else {
            //route.clear();
        }


        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }


    public void stopTracking() {
        tracking_active = false;
        route.clear();

        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListeners[0]);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }

        }
    }


}
