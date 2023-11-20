package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class TextValueObject implements Serializable {

    @Expose
    @SerializedName("text")
    String text;

    @Expose
    @SerializedName("value")
    String value;

    public TextValueObject(){}

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
