package com.example.dmitry.xplocity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Classes.Chain;
import Classes.ChainLocation;
import Classes.LocationCategory;
import misc.UTF8StringRequest;
import misc.VolleySingleton;
import XMLParsers.XMLLocationCategoryParser;
import XMLParsers.XMLLocationsParser;

public class NewChainActivity extends FragmentActivity implements OnMapReadyCallback {

    public Chain chain;

    private GoogleMap mMap;
    private Polyline track;

    private FusedLocationProviderClient mFusedLocationClient;

    private static final int REQUEST_PERMISSIONS = 100;
    private static final float DEFAULT_ZOOM = 15f;

    boolean boolean_permission;
    private boolean ready_for_tracking = false;
    private Timer mTimer;
    private long route_update_period = 1000;
    private Handler mHandler;

    Button btn_start_tracking;
    Button btn_stop_tracking;

    private static int time_slider_min = 30;
    private ArrayList<CheckBox> category_checkboxes;

    private boolean is_restoring = false;

    private ServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_chain);

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable("chain") != null) {
                chain = savedInstanceState.getParcelable("chain");
                ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
                Animation inAnimation = animator.getInAnimation();
                Animation outAnimation = animator.getOutAnimation();
                animator.setInAnimation(null);
                animator.setOutAnimation(null);
                animator.setDisplayedChild(1);
                animator.setInAnimation(inAnimation);
                animator.setOutAnimation(outAnimation);

                is_restoring = true;

            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // getMapAsync -> onMapReady -> init_service -> onServiceConnected ->


        init_chain_settings();

        btn_start_tracking = (Button) findViewById(R.id.btn_start_tracking);
        btn_stop_tracking = (Button) findViewById(R.id.btn_stop_tracking);

        if (savedInstanceState == null) {
            btn_start_tracking.setEnabled(false);
            btn_stop_tracking.setEnabled(false);
        }

        mHandler = new Handler();
        fn_permission();

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("chain", chain);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (boolean_permission) {
            enable_my_location();
            init_service();
        }
    }


    private void init_service() {
        if (mConnection == null) {
            mConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
                    mService = binder.getService();


                    if (is_restoring) {
                        mService.startTracking(); // TODO проверить, включен ли трэкинг
                        draw_chain();
                        start_timer();
                        is_restoring = false;
                    }
                    else {


                        ready_for_tracking = true;

                        btn_start_tracking.setEnabled(true);
                        btn_stop_tracking.setEnabled(false);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    ready_for_tracking = false;

                    btn_start_tracking.setEnabled(false);
                    btn_stop_tracking.setEnabled(false);
                }
            };
        }


        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    public void start_tracking(View view) {
        mService.startTracking();

        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                            }
                        }
                    });
        } catch (SecurityException e) {

        }

        start_timer();

        btn_start_tracking.setEnabled(false);
        btn_stop_tracking.setEnabled(true);


    }

    public void stop_tracking(View view) {
        mService.stopTracking();
        track.remove();
        track = null;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        btn_start_tracking.setEnabled(true);
        btn_stop_tracking.setEnabled(false);

    }

    private void start_timer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTaskToGetLocation(), 5, route_update_period);
        }
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    draw_route();
                    chain.check_location_reached();
                }
            });

        }
    }


    public void draw_route() {
        if (mService != null) {

            if (mService.route != null) {
                if (!mService.route.isEmpty()) {
                    if (track == null) {
                        track = mMap.addPolyline(new PolylineOptions()
                                .addAll(mService.route)
                                .width(5)
                                .color(Color.RED));
                    } else {
                        track.setPoints(mService.route);
                    }
                }
            }

            if (mService.last_position != null) {
                chain.position = mService.last_position;
            }
        }
    }


    private void enable_my_location() throws SecurityException {
        mMap.setMyLocationEnabled(true);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    LocationService mService;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */


    // Permissions
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                    enable_my_location();

                } else {
                    Toast.makeText(getApplicationContext(), "Xplocity needs the permission for access your position to track your routes.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    ////////// Chain settings windows

    public void init_chain_settings() {
        time_slider_init();
        download_location_categories("http://br-on.ru:3003/api/v1/location_categories");
    }


    public void create_chain(View view) {
        get_new_chain_info(this);
    }


    private void get_new_chain_info(final Context context) {


        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.

                            String urlString = generate_locations_url(location.getLatitude(), location.getLongitude());


                            // Formulate the request and handle the response.
                            StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, urlString,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            init_chain(response);
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // Handle error
                                        }
                                    });
                            VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
                        }
                    });
        } catch (SecurityException e) {

        }


    }

    private String generate_locations_url(double lat, double lon) {
        SeekBar sb = (SeekBar) findViewById(R.id.SelectTimeSlider);

        int loc_count = (sb.getProgress() + time_slider_min) / 15;
        double optimal_distance;
        RadioGroup travel_type = (RadioGroup) findViewById(R.id.travel_type);


        // get selected radio button from radioGroup
        int selectedId = travel_type.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_cycling) {
            optimal_distance = 3d;
        } else if (selectedId == R.id.radio_walking) {
            optimal_distance = 1d;
        } else if (selectedId == R.id.radio_running) {
            optimal_distance = 2d;
        } else {
            optimal_distance = 3d;
        }

        String url = "http://br-on.ru:3003/api/v1/locations/get_location_list?loc_count=" + Integer.toString(loc_count)
                + "&optimal_distance=" + Double.toString(optimal_distance) + "&latitude="
                + Double.toString(lat) + "&longitude=" + Double.toString(lon);

        for (CheckBox checkBox : category_checkboxes) {
            if (checkBox.isChecked()) {
                url = url + "&category[]=" + ((LocationCategory) checkBox.getTag()).id;
            }
        }

        return url;


    }

    private void init_chain(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationsParser parser = new XMLLocationsParser();

        try {
            ArrayList<ChainLocation> locations;
            locations = parser.parse(stream);

            chain = new Chain();
            chain.locations = locations;

            draw_chain();

            if (!chain.locations.isEmpty()) {
                ChainLocation loc = chain.locations.get(0);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc.position, 10f));
            }

            ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
            animator.showNext();

        } catch (Throwable e) {
            Log.e("chain", e.getMessage());
        }

    }


    private void draw_chain() {
        // add location markers to map
        for (ChainLocation loc : chain.locations) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(loc.position)
                    .title(loc.name)
                    .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " + loc.description));

            loc.marker = m;
            if (loc.explored)
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            m.setTag(loc);
        }

    }


    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + " h " + minutes + " m";
    }

    private void time_slider_init() {
        SeekBar time_slider = (SeekBar) findViewById(R.id.SelectTimeSlider);
        time_slider.setMax(1440 - time_slider_min);

        final TextView text_time = (TextView) findViewById(R.id.text_time);

        time_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_time.setText(formatHoursAndMinutes(progress + time_slider_min));
            }
        });

        time_slider.setProgress(210);
    }


    private void download_location_categories(String urlString) {

        // Formulate the request and handle the response.
        StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        location_categories_init(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    // Chain list
    private void location_categories_init(String xml) {

        ArrayList<LocationCategory> location_categories;
        category_checkboxes = new ArrayList<CheckBox>();

        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationCategoryParser parser = new XMLLocationCategoryParser();

        try {
            location_categories = parser.parse(stream);


            LinearLayout location_categories_list = (LinearLayout) findViewById(R.id.location_categories_list);
            for (LocationCategory cat : location_categories) {
                CheckBox checkbox = new CheckBox(this);
                checkbox.setTag(cat);
                checkbox.setText(cat.name);


                location_categories_list.addView(checkbox);
                category_checkboxes.add(checkbox);

            }

        } catch (Throwable e) {
            Log.e("chain", e.getMessage());
        }
    }


}
