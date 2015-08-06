package com.example.pro.traintimer.storage;

import com.example.pro.traintimer.model.Exercise;

import java.util.ArrayList;

public interface Storage {
    public void addExercise(Exercise exercise);
    public void getExercises();
    public void deleteExercises(ArrayList<Exercise> doDelete);
    public void updateExercise(Exercise exercise);
}
