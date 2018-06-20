package edu.uic.cs442.msms.model;

/**
 * Created by jun on 4/11/18.
 */

public class History {

    private int time;
    private double dangerLevels;
    private String type;

    public History(int t, double d, String typ){
        this.time = t;
        this.dangerLevels = d;
        this.type = typ;
    }

    public double getDangerLevels() {
        return dangerLevels;
    }
}
