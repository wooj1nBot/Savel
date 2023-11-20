package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionsViaWaypoint {

    @Expose
    @SerializedName("location")
    LatLngLiteral location;

    @Expose
    @SerializedName("step_index")
    int step_index;

    @Expose
    @SerializedName("step_interpolation")
    double step_interpolation;

    public DirectionsViaWaypoint(){}

    public double getStep_interpolation() {
        return step_interpolation;
    }

    public int getStep_index() {
        return step_index;
    }

    public LatLngLiteral getLocation() {
        return location;
    }
}
