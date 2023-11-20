package com.example.savel;

import java.io.Serializable;
import java.util.List;

public class Travel implements Serializable {

    List<Location> locations ;
    DirectionsResponse directionsResponse;
    TravelSetting travelSetting;
    public static final int TRAVEL_STATUS_BEFORE_START = 0;
    public static final int TRAVEL_STATUS_START = 1;
    public static final int TRAVEL_STATUS_END = 2;

    public static final int USER_STATUS_NOT_MOVING = -1;
    public static final int USER_STATUS_MOVING = 0;
    public static final int USER_STATUS_ARRIVAL = 1;
    public static final int USER_STATUS_WARNING = 2;
    public static final int USER_STATUS_NOT_WARNING = 3;
    public static final int USER_STATUS_ARRIVAL_CHECKPOINT = 4;

    int status = TRAVEL_STATUS_BEFORE_START;
    int user_status = USER_STATUS_NOT_MOVING;

    float distance = 0;

    public Travel(){}

    public Travel(List<Location> locations, DirectionsResponse directionsResponse, TravelSetting travelSetting, float distance){
        this.locations = locations;
        this.directionsResponse = directionsResponse;
        this.travelSetting = travelSetting;
        this.distance = distance;
    }

    public DirectionsResponse getDirectionsResponse() {
        return directionsResponse;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public TravelSetting getTravelSetting() {
        return travelSetting;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public float getDistance() {
        return distance;
    }

    public int getUser_status() {
        return user_status;
    }
}
