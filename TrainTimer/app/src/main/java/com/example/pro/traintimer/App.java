package com.example.pro.traintimer;

import android.app.Activity;
import android.app.Application;
import android.widget.ArrayAdapter;

import com.example.pro.traintimer.db.MyDBHelper;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.storage.CloudStorage;
import com.example.pro.traintimer.transport.ExerciseStateListener;
import com.example.pro.traintimer.transport.NetworkEventListener;
import com.example.pro.traintimer.transport.TimeScheduler;
import com.parse.Parse;
import com.parse.ParseACL;

import java.util.ArrayList;
import java.util.HashSet;

public class App extends Application implements NetworkEventListener,ExerciseStateListener {
    public static final int MAX_TIME=59;
    public static final int MIN_TIME=0;
    public static final int SONG=0;
    public static final int PLAYLIST=1;
    public static final int NO_MUSIC=2;

    private TimeScheduler timeScheduler;
    private static ArrayList<Exercise> exercises = new ArrayList<>();
    private static ArrayAdapter<String> setsAdapter;
    private CloudStorage cloudStorage;
    private HashSet<NetworkEventListener> listeners;
    private int toBeDeleted;
    private final Object object = new Object();
    private ArrayList<Exercise> deleted;
    private ArrayList<ExerciseStateListener> stateListeners;
    private MyDBHelper helper;

    public static ArrayList<Exercise> getExercises(){
        return exercises;
    }

    public void addListener(NetworkEventListener networkEventListener) {
        if (!listeners.contains(networkEventListener))
            listeners.add(networkEventListener);
    }

    public void removeListener(NetworkEventListener listener){
        if (listeners.contains(listener))
            listeners.remove(listener);
    }

    public void addStateListener(ExerciseStateListener listener){
        if (!stateListeners.contains(listener))
            stateListeners.add(listener);
    }

    public void removeStateListener(ExerciseStateListener listener){
        if (stateListeners.contains(listener))
            stateListeners.remove(listener);
    }

    public void startDownloading(){
        cloudStorage.getExercises();
    }

    public static void initAdapter(Activity activity,int resource,ArrayList<String> arrayList){
        setsAdapter = new ArrayAdapter<>(activity,resource,arrayList);
    }

    public static ArrayAdapter<String> getSetsAdapter(){
        return setsAdapter;
    }

    public void addExercise(Exercise exercise){
        cloudStorage.addExercise(exercise);
    }

    public void deleteExercises(ArrayList<Exercise> toDelete){
        if (toBeDeleted == 0) {
            toBeDeleted = toDelete.size();
            deleted = new ArrayList<>();
            cloudStorage.deleteExercises(toDelete);
        }
    }

    public void updateExercise(Exercise exercise){
        cloudStorage.updateExercise(exercise);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
    }

    private void initApp() {
        listeners = new HashSet<>();
        stateListeners = new ArrayList<>();
        cloudStorage = new CloudStorage();
        cloudStorage.setListener(this);
        Parse.initialize(this, getResources().getString(R.string.application_id),
                getResources().getString(R.string.client_key));
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        ParseACL.setDefaultACL(acl,true);
        cloudStorage.setResources(getResources());
        timeScheduler = new TimeScheduler(getApplicationContext(),this,getContentResolver(),this);
        helper = new MyDBHelper(this,1);
    }

    public void startExercise(Exercise exercise){
        timeScheduler.start(exercise);
    }

    public void stopExercise(){
        timeScheduler.stop();
    }

    public MyDBHelper getMyDBHelper(){
        return helper;
    }

    @Override
    public void onExerciseAdded(Exercise exercise) {
        if (exercise != null){
            synchronized (object) {
                exercises.add(exercise);
            }
        }

        for (NetworkEventListener listener: listeners)
            listener.onExerciseAdded(exercise);

    }

    @Override
    public void onExercisesDownloaded(ArrayList<Exercise> downloadedExercises) {
        if (downloadedExercises != null)
            exercises = downloadedExercises;
        for (NetworkEventListener listener : listeners)
            listener.onExercisesDownloaded(downloadedExercises);
    }

    @Override
    public void onExercisesDeleted(ArrayList<Exercise> deletedExercises, int shouldHaveBeenDeleted) {
        synchronized (object) {
            toBeDeleted--;
            if (deletedExercises != null) {
                Exercise e = deletedExercises.get(0);
                exercises.remove(e);
                deleted.add(e);
            }
            if (toBeDeleted == 0)
                for (NetworkEventListener listener : listeners)
                    listener.onExercisesDeleted(deleted, shouldHaveBeenDeleted);
        }
    }

    @Override
    public void onExerciseUpdated(Exercise exercise) {
        if (exercise != null){
            synchronized (object) {
                for (int i=0; i<exercises.size(); i++){
                    if (exercises.get(i).getID().equals(exercise.getID())){
                        exercises.set(i,exercise);
                        break;
                    }
                }
            }
        }

        for (NetworkEventListener listener: listeners)
            listener.onExerciseUpdated(exercise);
    }

    @Override
    public void onExerciseEnded() {
        for (ExerciseStateListener listener: stateListeners)
            listener.onExerciseEnded();
    }
}
