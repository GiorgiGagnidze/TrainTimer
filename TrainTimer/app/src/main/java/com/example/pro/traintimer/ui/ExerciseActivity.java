package com.example.pro.traintimer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.transport.ExerciseStateListener;
import com.example.pro.traintimer.transport.NetworkEventListener;

import java.util.ArrayList;


public class ExerciseActivity extends Activity implements NetworkEventListener,ExerciseStateListener{
    private App app;
    private Exercise exercise;
    private boolean exerciseState = false;
    private TextView exerciseName;
    private TextView playlistName;
    private TextView songName;
    private TextView info;
    private TextView relaxTime;
    private RadioGroup radioGroup;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = getIntent().getExtras().getInt(getResources().getString(R.string.key_index));
        exercise = App.getExercises().get(index);
        app =(App)getApplication();
        app.addListener(this);
        app.addStateListener(this);
        setContentView(R.layout.set_listview);
        View header = getLayoutInflater().inflate(R.layout.activity_exercise, null);
        exerciseName = (TextView)header.findViewById(R.id.textView_name);
        playlistName = (TextView)header.findViewById(R.id.textView_playlist);
        songName = (TextView)header.findViewById(R.id.textView_song);
        info = (TextView)header.findViewById(R.id.textView_info);
        relaxTime = (TextView)header.findViewById(R.id.textView_relax_time);
        radioGroup = (RadioGroup)header.findViewById(R.id.group);
        exerciseName.setText(exercise.getExerciseName());
        playlistName.setText(exercise.getPlaylistName());
        songName.setText(exercise.getSongName());
        info.setText(exercise.getInfo());
        relaxTime.setText(exercise.getRelaxTime().substring(3));
        RadioButton button=(RadioButton)radioGroup.getChildAt(exercise.getMusicOption());
        button.setChecked(true);
        for (int i=0; i<radioGroup.getChildCount(); i++)
            radioGroup.getChildAt(i).setEnabled(false);
        listView = (ListView)findViewById(R.id.added_sets_list_view);
        listView.addHeaderView(header);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,
                exercise.getSets());
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeListener(this);
        app.removeStateListener(this);
    }

    @Override
    public void onExercisesDownloaded(ArrayList<Exercise> exercises) {
    }

    @Override
    public void onExerciseAdded(Exercise exercise) {
        if (exercise == null)
            Toast.makeText(this, getResources().getString(R.string.add_alert),
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExerciseUpdated(Exercise exercise) {
        if (exercise == null)
            Toast.makeText(this, getResources().getString(R.string.update_alert),
                    Toast.LENGTH_LONG).show();
        else if (this.exercise.getID().equals(exercise.getID())){
            this.exercise = exercise;
            exerciseName.setText(exercise.getExerciseName());
            playlistName.setText(exercise.getPlaylistName());
            songName.setText(exercise.getSongName());
            info.setText(exercise.getInfo());
            relaxTime.setText(exercise.getRelaxTime().substring(3));
            RadioButton button=(RadioButton)radioGroup.getChildAt(exercise.getMusicOption());
            button.setChecked(true);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,
                    exercise.getSets());
            listView.setAdapter(adapter);
        }

    }

    @Override
    public void onExercisesDeleted(ArrayList<Exercise> exercises, int shouldHaveBeenDeleted) {
        for (Exercise e: exercises){
            if (e.getID().equals(exercise.getID()))
                finish();
        }
        int left = shouldHaveBeenDeleted - exercises.size();
        if (left != 0)
            Toast.makeText(this, left+getResources().getString(R.string.delete_alert),
                    Toast.LENGTH_LONG).show();
    }

    public void stop(View view) {
        if (exerciseState) {
            app.stopExercise();
            exerciseState = false;
        }
    }

    public void start(View view) {
        if (!exerciseState){
            exerciseState = true;
            app.startExercise(exercise);
        }
    }

    public void update(View view) {
        if (!exerciseState){
            Intent intent = new Intent(this, UpdateActivity.class);
            intent.putExtra(this.getResources().getString(R.string.key_index),getIntent().getExtras()
                    .getInt(getResources().getString(R.string.key_index)));
            startActivity(intent);
        } else
            Toast.makeText(this, getResources().getString(R.string.running_alert),
                    Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getResources().getString(R.string.key_state),exerciseState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        exerciseState = savedInstanceState.getBoolean(getResources().getString(R.string.key_state));
    }

    @Override
    public void onExerciseEnded() {
        exerciseState = false;
    }


}
