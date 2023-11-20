package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LatLngLiteral implements Serializable {

    @Expose
    @SerializedName("lat")
    double lat;

    @Expose
    @SerializedName("lng")
    double lng;

    public LatLngLiteral(){}

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
