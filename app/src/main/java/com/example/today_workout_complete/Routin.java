package com.example.today_workout_complete;

import java.io.Serializable;
import java.util.List;

public class Routin implements Serializable {
    String routinName;
    List<Exercise> exercises;
    int lastExerciseTimeTook;
    String lastExerciseDate;

    public Routin(String routinName, List<Exercise> exercises, int lastExerciseTimeTook, String lastExerciseDate){
        this.routinName = routinName;
        this.exercises = exercises;
        this.lastExerciseTimeTook = lastExerciseTimeTook;
        this.lastExerciseDate = lastExerciseDate;
    }

    public String getRoutinName() {
        return routinName;
    }
    public List<Exercise> getExercises() {
        return exercises;
    }
    public int getLastExerciseTimeTook() {
        return lastExerciseTimeTook;
    }
    public String getLastExerciseDate() {
        return lastExerciseDate;
    }

    public void setRoutinName(String routinName) {
        this.routinName = routinName;
    }
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
    public void setLastExerciseTimeTook(int lastExerciseTimeTook) {
        this.lastExerciseTimeTook = lastExerciseTimeTook;
    }
    public void setLastExerciseDate(String lastExerciseDate) {
        this.lastExerciseDate = lastExerciseDate;
    }
}
