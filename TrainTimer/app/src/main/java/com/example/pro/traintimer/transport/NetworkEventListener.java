package com.example.pro.traintimer.transport;

import com.example.pro.traintimer.model.Exercise;

import java.util.ArrayList;


public interface NetworkEventListener {
    public void onExercisesDownloaded(ArrayList<Exercise> exercises);
    public void onExerciseAdded(Exercise exercise);
    public void onExercisesDeleted(ArrayList<Exercise> exercises, int shouldHaveBeenDeleted);
    public void onExerciseUpdated(Exercise exercise);
}
