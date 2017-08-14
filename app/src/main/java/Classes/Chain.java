package Classes;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by dmitry on 02.08.17.
 */

public class Chain implements Parcelable {
    public String date;
    public int loc_cnt_explored;
    public int loc_cnt_total;
    public int id;

    public ArrayList<LatLng> route;
    public ArrayList<ChainLocation> locations;

    public LatLng position;

    private static final int LOCATION_REACHED_DISTANCE = 50;

    public Chain() {

        locations = new ArrayList<ChainLocation>();
        route = new ArrayList<LatLng>();
    }

    public Chain(int id, String date, int loc_cnt_explored, int loc_cnt_total) {
        this.id = id;
        this.date = date;
        this.loc_cnt_explored = loc_cnt_explored;
        this.loc_cnt_total = loc_cnt_total;

        locations = new ArrayList<ChainLocation>();
        route = new ArrayList<LatLng>();
    }


    public static ArrayList<LatLng> string_to_route(String str_route) {
        ArrayList<LatLng> points = new ArrayList<LatLng>();

        for (String str_pos : str_route.split(";")) {
            //Achtung! API возвращает координаты в инвертированном порядке. (Долгота-ширина)
            Double longitude = Double.parseDouble(str_pos.substring(0, str_pos.indexOf(" ")));
            Double latitude = Double.parseDouble(str_pos.substring(str_pos.indexOf(" ")+1, str_pos.length()));

            points.add(new LatLng(latitude, longitude));
        }

        return points;
    }



    public void check_location_reached() {
        if (position != null) {
            for (ChainLocation loc : locations) {
                if (!loc.explored) {
                    float[] results = new float[2];
                    Location.distanceBetween(loc.position.latitude, loc.position.longitude, position.latitude, position.longitude, results);
                    if (results[0] <= LOCATION_REACHED_DISTANCE) {
                        loc.explored = true;
                        loc.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }
            }
        }
    }



    /// Parcelable

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
        //builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.create();
        String json = gson.toJson(this);
        out.writeString(json);
    }

    public static final Parcelable.Creator<Chain> CREATOR
            = new Parcelable.Creator<Chain>() {
        public Chain createFromParcel(Parcel in) {
            return new Chain(in);
        }

        public Chain[] newArray(int size) {
            return new Chain[size];
        }
    };

    /** recreate object from parcel */
    private Chain(Parcel in) {
        Gson gson = new Gson();
        Chain c = gson.fromJson(in.readString(), Chain.class);
        this.id = c.id;
        this.date = c.date;
        this.route = c.route;
        this.locations = c.locations;
        this.loc_cnt_explored = c.loc_cnt_explored;
        this.loc_cnt_total = c.loc_cnt_total;
    }

}
