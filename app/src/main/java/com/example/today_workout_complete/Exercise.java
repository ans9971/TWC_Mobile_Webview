package com.example.today_workout_complete;

import java.io.Serializable;
import java.util.ArrayList;

public class Exercise implements Serializable {
    private String exerciseName;
    private int setCount;
    private ArrayList<Integer> reps;
    private int breakTime;

    public Exercise(String exerciseName, int setCount, ArrayList<Integer> reps, int breakTime){
        this.exerciseName = exerciseName;
        this.setCount = setCount;
        this.reps = reps;
        this.breakTime = breakTime;
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
