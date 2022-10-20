package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class Sets {
    @SerializedName("time")
    private long time;

    @SerializedName("break_time")
    private long break_time;

    @SerializedName("maximum_value_of_set")
    private float maximum_value_of_set;

    @SerializedName("minimum_value_of_set")
    private float minimum_value_of_set;

    @SerializedName("emg_data")
    private float[] emg_data;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBreak_time() {
        return break_time;
    }

    public void setBreak_time(long break_time) {
        this.break_time = break_time;
    }

    public float getMaximum_value_of_set() {
        return maximum_value_of_set;
    }

    public void setMaximum_value_of_set(float maximum_value_of_set) {
        this.maximum_value_of_set = maximum_value_of_set;
    }

    public float getMinimum_value_of_set() {
        return minimum_value_of_set;
    }

    public void setMinimum_value_of_set(float minimum_value_of_set) {
        this.minimum_value_of_set = minimum_value_of_set;
    }

    public float[] getEmg_data() {
        return emg_data;
    }

    public void setEmg_data(float[] emg_data) {
        this.emg_data = emg_data;
    }
}
