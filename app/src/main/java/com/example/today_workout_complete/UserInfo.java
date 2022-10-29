package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class UserInfo {



    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @SerializedName("nickname")
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public String getEmg_data_path() {
        return emg_data_path;
    }

    public void setEmg_data_path(String emg_data_path) {
        this.emg_data_path = emg_data_path;
    }

    @SerializedName("emg_data_path")
    private String emg_data_path;



    public String getCreation_datetime() {
        return creation_datetime;
    }

    public void setCreation_datetime(String creation_datetime) {
        this.creation_datetime = creation_datetime;
    }

    @SerializedName("creation_datetime")
    public String creation_datetime;


    public UserInfo(String nickname) {
        this.nickname = nickname;
    }







}
