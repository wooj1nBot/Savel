package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReverseGeoResult {

    @Expose
    @SerializedName("formatted_address")
    String formatted_address;

}
