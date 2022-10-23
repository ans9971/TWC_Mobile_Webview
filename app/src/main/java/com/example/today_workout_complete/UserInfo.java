package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class UserInfo {


//    private String member_id;
//    private String mail;
//    private String password;
//    private String user_name;
//    private String introdution;
//    private Integer phonenumber;
//    private String address;
//    private String sex;

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

//    @Override
//    public String toStirng(){
//        return "Result{"+
//                "nickname"+nickname+"}";
//    }
//    private String profile_image_path;
//    private String grantion_level;
//
//    public UserInfo(String nickname) {
//        this.nickname = nickname;
//    }
//
//    private String creation_datetime;
//    private String last_login_datetime;
//    private String password_changed_datetime;
//    private String deletion_datetime;
//    private String mail_reception;
//    private String authentication_status;






}
