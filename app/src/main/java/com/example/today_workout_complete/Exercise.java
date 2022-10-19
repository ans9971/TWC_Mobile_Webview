package com.example.today_workout_complete;

import java.io.Serializable;
import java.util.ArrayList;

public class Exercise implements Serializable {
    private String exerciseName;
    private int setCount;
    private ArrayList<Integer> reps;
    private int breakTime;
    private String measuredMuscle;

    public Exercise(String exerciseName, int setCount, ArrayList<Integer> reps, int breakTime, String measuredMuscle){
        this.exerciseName = exerciseName;
        this.setCount = setCount;
        this.reps = reps;
        this.breakTime = breakTime;
        this.measuredMuscle = measuredMuscle;
    }

    public String getExerciseName() {
        return exerciseName;
    }
    public int getSetCount() {
        return setCount;
    }
    public ArrayList<Integer> getReps() {
        return reps;
    }
    public int getBreak_time() {
        return breakTime;
    }
    public String getMeasuredMuscle() {
        return measuredMuscle;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    public void setSetCount(int setCount) {
        this.setCount = setCount;
    }
    public void setReps(ArrayList<Integer> reps) {
        this.reps = reps;
    }
    public void setBreak_time(int breakTime) {
        this.breakTime = breakTime;
    }
}
