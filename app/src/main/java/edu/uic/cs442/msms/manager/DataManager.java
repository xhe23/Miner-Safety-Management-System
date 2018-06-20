package edu.uic.cs442.msms.manager;

import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;

import edu.uic.cs442.msms.model.DangerZone;
import edu.uic.cs442.msms.model.History;

/**
 * Created by jun on 3/29/18.
 */

public class DataManager {

    private static DataManager manager;
    private static final Object lock = new Object();

    // List of Data which will be used in APP
    private ArrayList<MarkerOptions> markerOptions;
    private ArrayList<Marker> markers;
    private ArrayList<History> histories;
    private HashSet<String> dangers;
    private float[][] tables;

    public static synchronized DataManager getInstance(){

        DataManager r = manager;

        if(r == null){

            synchronized (lock){
                r = manager;

                if(r == manager){
                    r = new DataManager();
                    manager = r;
                }
            }
        }

        return r;
    }

    private DataManager(){
        //Log.d("DataManager", "Should be called one time");
        markerOptions = new ArrayList<>();
        markers = new ArrayList<>();
        histories = new ArrayList<>();
        dangers = new HashSet<>();
    }

    public void setTable(float[][] t){
        this.tables = t;
    }

    public float[][] getTables(){
        return this.tables;
    }

    public void addDangerValue(String val){
        dangers.add(val);
    }

    public boolean containsDanger(String val){
        return dangers.contains(val);
    }

    public void addHistory(History history){
        this.histories.add(history);
    }

    public int getHistorySize(){
        return histories.size();
    }

    public ArrayList<History> getHistories() {
        return histories;
    }

    public void addDangerZone(DangerZone zone){
        markerOptions.add(new MarkerOptions().position(zone.getLatLng()).title(zone.getTitle()).snippet(zone.getSnippet()));
    }

    public MarkerOptions getMarkerOption(DangerZone zone){
        for(MarkerOptions markerOption : markerOptions){
            if(markerOption.getPosition() == zone.getLatLng()){
                return markerOption;
            }
        }
        return null;
    }

    public ArrayList<MarkerOptions> getMarkerOptions() {
        return markerOptions;
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public void addMarkers(Marker marker) {
        marker.setVisible(false);
        this.markers.add(marker);
    }
}
