package com.example.savel;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;


public class CustomLatLng {

    public double latitude;
    public double longitude;

    public CustomLatLng(){}

    public CustomLatLng(double latitude, double longitude) {
         this.latitude = latitude;
         this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    LatLng gLatLng(){
        return new LatLng(latitude, longitude);
    }

    void sLatLng(LatLng latLng){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }
}
