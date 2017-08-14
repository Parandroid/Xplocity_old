package Classes;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by dmitry on 03.08.17.
 */

public class ChainLocation {
    public int id;
    public boolean explored;

    public String name;
    public String description;
    public String address;

    public LatLng position;

    public transient Marker marker;


    public ChainLocation() {

    }
}
