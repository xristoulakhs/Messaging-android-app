package com.example.distributedsystemsapp.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileName implements Serializable {

    private String profileName;

    public ProfileName(String profileName, ArrayList<String> subscribedConversations) {
        this.profileName = profileName;
    }

    public ProfileName() {
    }

    public ProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @Override
    public String toString() {
        return "ProfileName{" +
                "profileName='" + profileName + '\'' +
                '}';
    }
}
