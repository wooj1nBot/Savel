package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DirectionsResponse implements Serializable{
    @Expose
    @SerializedName("routes")
    List<DirectionsRoute> routes;

    @Expose
    @SerializedName("status")
    String status;

    public DirectionsResponse() {}

    public List<DirectionsRoute> getRoutes() {
        return routes;
    }

    public String getStatus() {
        return status;
    }
}
