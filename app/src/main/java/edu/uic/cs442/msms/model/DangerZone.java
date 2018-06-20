package edu.uic.cs442.msms.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jun on 3/29/18.
 */

public class DangerZone {

    private LatLng latLng;
    private String title;
    private String snippet;

    public DangerZone(LatLng location, String t, String s){
        this.latLng = location;
        this.title = t;
        this.snippet = s;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }
}
