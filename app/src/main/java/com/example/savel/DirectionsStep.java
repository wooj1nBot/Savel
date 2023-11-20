package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

public class DirectionsStep implements Serializable {

    @Expose
    @SerializedName("duration")
    TextValueObject duration;

    @Expose
    @SerializedName("end_location")
    LatLngLiteral end_location;

    @Expose
    @SerializedName("start_location")
    LatLngLiteral start_location;

    @Expose
    @SerializedName("distance")
    TextValueObject distance;

    @Expose
    @SerializedName("html_instructions")
    String html_instructions;

    @Expose
    @SerializedName("maneuver")
    String maneuver;

    boolean isPassed = false;
    boolean isArrived = false;

    public DirectionsStep(){}

    public LatLngLiteral getStart_location() {
        return start_location;
    }

    public LatLngLiteral getEnd_location() {
        return end_location;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }

    public TextValueObject getDistance() {
        return distance;
    }

    public TextValueObject getDuration() {
        return duration;
    }

    public String getManeuver() {
        return maneuver;
    }

    public boolean getIsPassed() {
        return isPassed;
    }

    public boolean getIsArrived() {
        return isArrived;
    }

}
