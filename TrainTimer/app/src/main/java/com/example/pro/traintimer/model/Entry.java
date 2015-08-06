package com.example.pro.traintimer.model;

public class Entry {
    private Exercise exercise;
    private boolean isChecked;

    public Entry(Exercise exercise, boolean isChecked){
        this.exercise = exercise;
        this.isChecked = isChecked;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
