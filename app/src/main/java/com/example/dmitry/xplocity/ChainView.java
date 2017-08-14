package com.example.dmitry.xplocity;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import Adapters.LocationToMapAdapter;
import Classes.Chain;
import Classes.ChainLocation;
import misc.VolleySingleton;
import XMLParsers.XMLChainParser;

public class ChainView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public int chain_id;
    public Chain chain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Bundle recdData = getIntent().getExtras();
        chain_id =  recdData.getInt("chain_id");

        download_chain("http://br-on.ru:3003/api/v1/chains/"+Integer.toString(chain_id));
    }

    private void download_chain(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        chain_init(response);
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
    private void chain_init(String xml) {

        ArrayList<Chain> chains;

        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLChainParser ChainParser = new XMLChainParser();

        try {
            chain = ChainParser.parse(stream);
            // используем адаптер данных
            if (mMap != null) {
                draw_chain();
            }
        }
        catch (Throwable e) {
            Log.e("chain", e.getMessage());
        }
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
        mMap.setInfoWindowAdapter(new LocationToMapAdapter(this.getBaseContext()));

        if (chain != null) {
            draw_chain();
        }

    }


    private void draw_chain() {
        // add location markers to map
        for (ChainLocation loc : chain.locations) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(loc.position)
                    .title(loc.name)
                    .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " +loc.description));
            if (loc.explored)
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            m.setTag(loc);
        }

        if (!chain.locations.isEmpty()) {
            ChainLocation loc = chain.locations.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc.position, 10f));
        }

        if (!chain.route.isEmpty()) {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(chain.route)
                    .width(5)
                    .color(Color.RED));

        }
    }
}
