package com.example.pro.traintimer.storage;

import android.content.res.Resources;
import android.util.Log;

import com.example.pro.traintimer.R;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.transport.NetworkEventListener;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CloudStorage implements Storage {
    private Resources resources;
    private static final String DELIMITER="/";
    private NetworkEventListener listener;

    @Override
    public void updateExercise(final Exercise exercise){
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(resources.getString(
                R.string.table_exercises));
        query.whereEqualTo(resources.getString(R.string.key_user), user);
        query.getInBackground(exercise.getID(),new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e==null){
                    Log.i("ipova",exercise.getInfo());
                    object.put(resources.getString(R.string.key_exercise_name),exercise.getExerciseName());
                    object.put(resources.getString(R.string.key_info),exercise.getInfo());
                    object.put(resources.getString(R.string.key_song_name),exercise.getSongName());
                    object.put(resources.getString(R.string.key_playlist_name),exercise.getPlaylistName());
                    object.put(resources.getString(R.string.key_relax_time),exercise.getRelaxTime());
                    object.put(resources.getString(R.string.key_music_option),exercise.getMusicOption());
                    String sets = "";
                    for (String s: exercise.getSets())
                        sets += s+DELIMITER;
                    object.put(resources.getString(R.string.key_sets),sets);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null) {
                                Log.i("chawera","chawera");
                                listener.onExerciseUpdated(exercise);
                            }else
                                listener.onExerciseUpdated(null);
                        }
                    });
                } else
                    listener.onExerciseUpdated(null);
            }
        });
    }

    @Override
    public void addExercise(final Exercise exercise) {
        final ParseObject object = new ParseObject(resources.getString(R.string.table_exercises));
        object.put(resources.getString(R.string.key_exercise_name),exercise.getExerciseName());
        object.put(resources.getString(R.string.key_info),exercise.getInfo());
        object.put(resources.getString(R.string.key_song_name),exercise.getSongName());
        object.put(resources.getString(R.string.key_playlist_name),exercise.getPlaylistName());
        object.put(resources.getString(R.string.key_relax_time),exercise.getRelaxTime());
        object.put(resources.getString(R.string.key_music_option),exercise.getMusicOption());
        String sets = "";
        for (String s: exercise.getSets())
            sets += s+DELIMITER;
        object.put(resources.getString(R.string.key_sets),sets);
        object.put(resources.getString(R.string.key_user), ParseUser.getCurrentUser());

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    exercise.setID(object.getObjectId());
                    listener.onExerciseAdded(exercise);
                } else
                    listener.onExerciseAdded(null);
            }
        });
    }

    @Override
    public void getExercises() {
        final ArrayList<Exercise> exercises = new ArrayList<>();
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(resources.getString(
                R.string.table_exercises));
        query.whereEqualTo(resources.getString(R.string.key_user), user);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : parseObjects) {
                        StringTokenizer st = new StringTokenizer(object.getString(resources.getString
                                (R.string.key_sets)),DELIMITER);
                        ArrayList<String> sets = new ArrayList<>();
                        while (st.hasMoreTokens())
                            sets.add(st.nextToken());

                        Exercise exercise = new Exercise(
                                object.getString(resources.getString(R.string.key_exercise_name)),
                                object.getString(resources.getString(R.string.key_playlist_name)),
                                object.getString(resources.getString(R.string.key_song_name)),
                                object.getString(resources.getString(R.string.key_info)),
                                object.getInt(resources.getString(R.string.key_music_option)),
                                object.getString(resources.getString(R.string.key_relax_time)),
                                sets
                        );
                        exercise.setID(object.getObjectId());
                        exercises.add(exercise);
                    }
                    listener.onExercisesDownloaded(exercises);
                } else
                    listener.onExercisesDownloaded(null);
            }
        });
    }

    @Override
    public void deleteExercises(final ArrayList<Exercise> toDelete) {
        ParseUser user = ParseUser.getCurrentUser();
        for (Exercise e: toDelete){
            final Exercise exercise = e;
            ParseQuery<ParseObject> query = ParseQuery.getQuery(resources.getString(
                    R.string.table_exercises));
            query.whereEqualTo(resources.getString(R.string.key_user), user);
            query.getInBackground(e.getID(),new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        parseObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    ArrayList<Exercise> deleted = new ArrayList<>();
                                    deleted.add(exercise);
                                    listener.onExercisesDeleted(deleted,toDelete.size());
                                } else
                                    listener.onExercisesDeleted(null,toDelete.size());
                            }
                        });
                    } else
                        listener.onExercisesDeleted(null, toDelete.size());
                }
            });
        }
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public void setListener(NetworkEventListener listener) {
        this.listener = listener;
    }
}
