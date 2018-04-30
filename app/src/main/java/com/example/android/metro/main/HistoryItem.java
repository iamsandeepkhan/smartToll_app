package com.example.android.metro.main;

/**
 * Class to store the details of a completed journey.
 * @author Sandeep Khan
 */

public class HistoryItem {
    public String startStation;
    public String endStation;
    public String entryTime;
    public String exitTime;
    public String tourID;
    public double amount;
    public HistoryItem(String startStation, String endStation,String entryTime,String exitTime, double amount,String tourID){
        this.startStation=startStation;
        this.endStation = endStation;
        this.amount = amount;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.tourID = tourID;
    }
}
