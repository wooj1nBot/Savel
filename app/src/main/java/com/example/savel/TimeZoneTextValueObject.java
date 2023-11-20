package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TimeZoneTextValueObject implements Serializable {
    @Expose
    @SerializedName("text")
    String text;
    @Expose
    @SerializedName("time_zone")
    String time_zone;
    @Expose
    @SerializedName("value")
    long value;
}
