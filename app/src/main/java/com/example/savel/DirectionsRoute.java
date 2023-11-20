package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DirectionsRoute implements Serializable {
    @Expose
    @SerializedName("legs")
    List<DirectionsLeg> legs;

    @Expose
    @SerializedName("warnings")
    List<String> warnings;

    public DirectionsRoute(){}

    public List<DirectionsLeg> getLegs() {
        return legs;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
