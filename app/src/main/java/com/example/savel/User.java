package com.example.savel;

import java.io.Serializable;

public class User implements Serializable {
    String name;
    String email;
    String uid;
    String profile;
    String token;

    public User() {}

    public User(String name, String email, String uid, String profile, String token){
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.profile = profile;
        this.token = token;
    }


    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public String getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }
}
