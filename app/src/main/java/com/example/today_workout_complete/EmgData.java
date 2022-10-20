package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmgData {
    @SerializedName("nickname")
    private String nickname;

    @SerializedName("workout_name")
    private String workout_name;

    @SerializedName("measured_muscle")
    private String measured_muscle;

    @SerializedName("starting_time")
    private String starting_time;

    @SerializedName("sensing_interval")
    private int sensing_interval;

    @SerializedName("number_of_sets")
    private int number_of_sets;

    @SerializedName("total_workout_time")
    private int total_workout_time;

    @SerializedName("maximum_value_of_sets")
    private int maximum_value_of_sets;

    @SerializedName("minimum_value_of_sets")
    private int minimum_value_of_sets;

    @SerializedName("sets")
    private List<Sets> sets;


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getWorkout_name() {
        return workout_name;
    }

    public void setWorkout_name(String workout_name) {
        this.workout_name = workout_name;
    }

    public String getMeasured_muscle() {
        return measured_muscle;
    }

    public void setMeasured_muscle(String measured_muscle) {
        this.measured_muscle = measured_muscle;
    }

    public String getStarting_time() {
        return starting_time;
    }

    public void setStarting_time(String starting_time) {
        this.starting_time = starting_time;
    }

    public int getSensing_interval() {
        return sensing_interval;
    }

    public void setSensing_interval(int sensing_interval) {
        this.sensing_interval = sensing_interval;
    }

    public int getNumber_of_sets() {
        return number_of_sets;
    }

    public void setNumber_of_sets(int number_of_sets) {
        this.number_of_sets = number_of_sets;
    }

    public int getTotal_workout_time() {
        return total_workout_time;
    }

    public void setTotal_workout_time(int total_workout_time) {
        this.total_workout_time = total_workout_time;
    }

    public int getMaximum_value_of_sets() {
        return maximum_value_of_sets;
    }

    public void setMaximum_value_of_sets(int maximum_value_of_sets) {
        this.maximum_value_of_sets = maximum_value_of_sets;
    }

    public int getMinimum_value_of_sets() {
        return minimum_value_of_sets;
    }

    public void setMinimum_value_of_sets(int minimum_value_of_sets) {
        this.minimum_value_of_sets = minimum_value_of_sets;
    }

    public List<Sets> getSets() {
        return sets;
    }

    public void setSets(List<Sets> sets) {
        this.sets = sets;
    }


}
