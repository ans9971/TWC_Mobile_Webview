package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class UserInfo {




    @SerializedName("nickname")
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public String getEmg_data_path() {
        return emg_data_path;
    }


    @SerializedName("emg_data_path")
    private String emg_data_path;



    public String getCreation_datetime() {
        return creation_datetime;
    }



    @SerializedName("creation_datetime")
    public String creation_datetime;


    public UserInfo(String nickname) {
        this.nickname = nickname;
    }







}
