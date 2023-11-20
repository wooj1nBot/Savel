package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class DirectionsLeg implements Serializable {
    @Expose
    @SerializedName("end_address")
    String end_address;
    @Expose
    @SerializedName("end_location")
    LatLngLiteral end_location;
    @Expose
    @SerializedName("start_address")
    String start_address;
    @Expose
    @SerializedName("start_location")
    LatLngLiteral start_location;
    @Expose
    @SerializedName("steps")
    List<DirectionsStep> steps;
    @Expose
    @SerializedName("distance")
    TextValueObject distance;
    @Expose
    @SerializedName("duration")
    TextValueObject duration;
    @Expose
    @SerializedName("via_waypoint")
    List<DirectionsViaWaypoint> via_waypoint;


    public DirectionsLeg(){}

    public LatLngLiteral getEnd_location() {
        return end_location;
    }

    public LatLngLiteral getStart_location() {
        return start_location;
    }

    public List<DirectionsStep> getSteps() {
        return steps;
    }

    public String getEnd_address() {
        return end_address;
    }

    public String getStart_address() {
        return start_address;
    }

    public TextValueObject getDuration() {
        return duration;
    }

    public TextValueObject getDistance() {
        return distance;
    }

    public List<DirectionsViaWaypoint> getVia_waypoint() {
        return via_waypoint;
    }
}
