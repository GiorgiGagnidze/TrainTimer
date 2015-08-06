package com.example.pro.traintimer.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;
import com.example.pro.traintimer.adapters.DeleteListAdapter;
import com.example.pro.traintimer.model.Entry;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.transport.NetworkEventListener;

import java.util.ArrayList;


public class DeleteActivity extends Activity implements NetworkEventListener {
    private App app;
    private EditText editText;
    private ListView listView;
    private DeleteListAdapter adapter;
    private ArrayList<String> checkedEntriesIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        app =(App)getApplication();
        app.addListener(this);
        listView = (ListView)findViewById(R.id.to_delete_list_view);
        editText = (EditText)findViewById(R.id.search_to_delete);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                fillList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        fillList(editText.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> IDs =new ArrayList<>();
        for (Exercise e: adapter.getCheckedExercises())
            IDs.add(e.getID());
        outState.putStringArrayList(getResources().getString(R.string.key_array),IDs);
        outState.putString(getResources().getString(R.string.key_search),editText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        editText.setText(savedInstanceState.getString(getResources().getString(R.string.key_search)));
        ArrayList<String> IDs = savedInstanceState.getStringArrayList(getResources().getString(
                R.string.key_array));
        fillList(editText.getText().toString());
        ArrayList<Entry> entries = adapter.getEntries();
        for (String id:IDs){
            for (Entry e: entries){
                if (e.getExercise().getID().equals(id)){
                    e.setChecked(true);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    private void fillList(String keyWord){
        ArrayList<Entry> entries = new ArrayList<>();
        for (Exercise e: App.getExercises()){
            if (e.getExerciseName().toLowerCase().contains(keyWord.toLowerCase()))
                entries.add(new Entry(e,false));
        }
        adapter = new DeleteListAdapter(entries,getApplicationContext());
        listView.setAdapter(adapter);
    }

    @Override
    public void onExerciseAdded(Exercise exercise) {
        if (exercise != null)
            fillList(editText.getText().toString());
        else
            Toast.makeText(this, getResources().getString(R.string.add_alert),
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExercisesDeleted(ArrayList<Exercise> exercises,int shouldHaveBeenDeleted) {
        int left = shouldHaveBeenDeleted - exercises.size();
        if (left == 0)
            finish();
        else {
            fillList(editText.getText().toString());
            Toast.makeText(this, left+getResources().getString(R.string.delete_alert),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onExercisesDownloaded(ArrayList<Exercise> exercises) {
        if (exercises != null)
            fillList(editText.getText().toString());
        else
            Toast.makeText(this, getResources().getString(R.string.download_alert),
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExerciseUpdated(Exercise exercise) {
        if (exercise != null)
            fillList(editText.getText().toString());
        else
            Toast.makeText(this, getResources().getString(R.string.update_alert),
                    Toast.LENGTH_LONG).show();
    }

    public void delete(View view) {
        ArrayList<Exercise> exercises = adapter.getCheckedExercises();
        if (exercises.size()>0)
            app.deleteExercises(exercises);
    }
}
