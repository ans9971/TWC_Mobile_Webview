package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class EmgDataModel {

    @SerializedName("nickname")
    String nickname;

    @SerializedName("creation_datetime")
    String creation_datetime;

    public String getNickname() {
        return nickname;
    }

    public String getCreation_datetime() {
        return creation_datetime;
    }





}
