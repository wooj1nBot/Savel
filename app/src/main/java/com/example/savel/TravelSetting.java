package com.example.savel;

import java.io.Serializable;
import java.util.Date;

public class TravelSetting implements Serializable {
    User traveler;
    User guardian;
    Date departureTime;
    String purpose;
    Date arrivalTime;

    public TravelSetting() {}
    public TravelSetting(User traveler, User guardian, Date departureTime, Date arrivalTime, String purpose){
        this.traveler = traveler;
        this.guardian = guardian;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.purpose = purpose;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public User getGuardian() {
        return guardian;
    }

    public User getTraveler() {
        return traveler;
    }

}
