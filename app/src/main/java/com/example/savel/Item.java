package com.example.savel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {
    @Expose
    @SerializedName("title")
    String title;

    @Expose
    @SerializedName("link")
    String link;

    @Expose
    @SerializedName("category")
    String category;

    @Expose
    @SerializedName("description")
    String description;

    @Expose
    @SerializedName("address")
    String address;

    @Expose
    @SerializedName("roadAddress")
    String roadAddress;

    @Expose
    @SerializedName("mapx")
    int mapx = -1;

    @Expose
    @SerializedName("mapy")
    int mapy = -1;
}
