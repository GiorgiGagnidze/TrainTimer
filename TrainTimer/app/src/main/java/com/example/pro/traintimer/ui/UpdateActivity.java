package com.example.pro.traintimer.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;
import com.example.pro.traintimer.fragments.RelaxTimeDialogFragment;
import com.example.pro.traintimer.fragments.SetDialogFragment;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.transport.NetworkEventListener;

import java.util.ArrayList;

public class UpdateActivity extends Activity implements NetworkEventListener{
    private ArrayList<String> sets;
    private ListView listView;
    private ArrayList<String> relaxTime = new ArrayList<>();
    private EditText exerciseName;
    private EditText playlistName;
    private EditText songName;
    private EditText info;
    private App app;
    private Exercise exercise;
    private int checkedButtonID;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = getIntent().getExtras().getInt(getResources().getString(R.string.key_index));
        exercise = App.getExercises().get(index);

        sets = exercise.getSets();
        app =(App)getApplication();
        app.addListener(this);
        setContentView(R.layout.set_listview);
        View header = getLayoutInflater().inflate(R.layout.new_exercise, null);
        exerciseName = (EditText)header.findViewById(R.id.exercise_name);
        playlistName = (EditText)header.findViewById(R.id.playlist_name);
        songName = (EditText)header.findViewById(R.id.song_name);
        info = (EditText)header.findViewById(R.id.more);
        Button submit = (Button)header.findViewById(R.id.submit_but);
        radioGroup = (RadioGroup)header.findViewById(R.id.music);
        listView = (ListView)findViewById(R.id.added_sets_list_view);
        listView.addHeaderView(header);
        App.initAdapter(this,android.R.layout.simple_list_item_1,sets);
        listView.setAdapter(App.getSetsAdapter());
        final Activity activity = this;

        exerciseName.setText(exercise.getExerciseName());
        playlistName.setText(exercise.getPlaylistName());
        songName.setText(exercise.getSongName());
        info.setText(exercise.getInfo());
        RadioButton button =(RadioButton)radioGroup.getChildAt(exercise.getMusicOption());
        if (savedInstanceState == null)
            button.setChecked(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SetDialogFragment setDialogFragment = new SetDialogFragment();
                setDialogFragment.setSets(sets);
                setDialogFragment.setIndex(i-1);
                setDialogFragment.show(getFragmentManager(),getResources().getString(R.string.tag));
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name= exerciseName.getText().toString();
                if (name.equals("")){
                    Toast.makeText(activity, activity.getResources().getString(R.string.exercise_name_alert),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String relax = exercise.getRelaxTime();
                if (relaxTime.size() > 0)
                    relax = relaxTime.get(relaxTime.size()-1);

                int musicOption = App.SONG;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radio2:
                        musicOption = App.PLAYLIST;
                        break;
                    case R.id.radio3:
                        musicOption = App.NO_MUSIC;
                        break;
                }
                Exercise e = new Exercise(name,
                        playlistName.getText().toString(),
                        songName.getText().toString(),
                        info.getText().toString(),
                        musicOption,
                        relax,
                        sets);
                e.setID(exercise.getID());
                app.updateExercise(e);
            }
        });
    }

    public void showRelaxTimeDialog(View view) {
        RelaxTimeDialogFragment relaxTimeDialogFragment = new RelaxTimeDialogFragment();
        relaxTimeDialogFragment.setRelaxTime(relaxTime);
        relaxTimeDialogFragment.show(getFragmentManager(),getResources().getString(R.string.tag));
    }

    public void showNewSetDialog(View view) {
        SetDialogFragment setDialogFragment = new SetDialogFragment();
        setDialogFragment.setSets(sets);
        setDialogFragment.show(getFragmentManager(),getResources().getString(R.string.tag));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(getResources().getString(R.string.key_relax_time), relaxTime);
        outState.putStringArrayList(getResources().getString(R.string.key_sets), sets);
        outState.putString(getResources().getString(R.string.key_exercise_name),
                exerciseName.getText().toString());
        outState.putString(getResources().getString(R.string.key_song_name),
                songName.getText().toString());
        outState.putString(getResources().getString(R.string.key_playlist_name),
                playlistName.getText().toString());
        outState.putString(getResources().getString(R.string.key_info),
                info.getText().toString());
        outState.putInt(getResources().getString(R.string.key_checked_radio),
                radioGroup.getCheckedRadioButtonId());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        relaxTime=savedInstanceState.getStringArrayList(getResources().getString(R.string.key_relax_time));
        sets = savedInstanceState.getStringArrayList(getResources().getString(R.string.key_sets));
        App.initAdapter(this,android.R.layout.simple_list_item_1,sets);
        listView.setAdapter(App.getSetsAdapter());
        exerciseName.setText(savedInstanceState.getString(getResources().getString(R.string.
                key_exercise_name)));
        songName.setText(savedInstanceState.getString(getResources().getString(R.string.
                key_song_name)));
        playlistName.setText(savedInstanceState.getString(getResources().getString(R.string.
                key_playlist_name)));
        info.setText(savedInstanceState.getString(getResources().getString(R.string.
                key_info)));
        radioGroup.check(savedInstanceState.getInt(getResources().getString(R.string.key_checked_radio)));
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

    @Override
    public void onExerciseUpdated(Exercise exercise) {
        if (exercise == null)
            Toast.makeText(this, getResources().getString(R.string.update_alert),
                    Toast.LENGTH_LONG).show();
        else if (this.exercise.getID().equals(exercise.getID()))
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeListener(this);
    }
}
