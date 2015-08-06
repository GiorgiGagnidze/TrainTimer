package com.example.pro.traintimer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pro.traintimer.fragments.LoginDialogFragment;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.transport.NetworkEventListener;
import com.example.pro.traintimer.ui.AddExerciseActivity;
import com.example.pro.traintimer.ui.DeleteActivity;
import com.example.pro.traintimer.ui.ExerciseActivity;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements NetworkEventListener {
    private App app;
    private EditText editText;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
            login();
        app =(App)getApplication();
        app.addListener(this);
        if (App.getExercises().size() == 0)
            app.startDownloading();
        listView = (ListView)findViewById(R.id.exercises_list_view);
        editText = (EditText)findViewById(R.id.search);
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
        final Activity activity = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(activity, ExerciseActivity.class);
                intent.putExtra(activity.getResources().getString(R.string.key_index),i);
                startActivity(intent);
            }
        });
    }

    private void fillList(String keyWord){
        ArrayList<String> exercises = new ArrayList<>();
        for (Exercise e: App.getExercises()){
            if (e.getExerciseName().toLowerCase().contains(keyWord.toLowerCase()))
                exercises.add(e.getExerciseName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,exercises);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillList(editText.getText().toString());
        app.stopExercise();
    }

    private void login(){
        SharedPreferences prefs = this.getSharedPreferences(
                getResources().getString(R.string.key_app), Context.MODE_PRIVATE);
        String defValue = "";
        String account = prefs.getString(getResources().getString(R.string.key_account),defValue);
        if (account.equals(defValue)){
            LoginDialogFragment login = new LoginDialogFragment();
            login.show(getFragmentManager(), getResources().getString(R.string.tag));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        fillList(editText.getText().toString());
        if (left != 0){
            fillList(editText.getText().toString());
            Toast.makeText(this, left+getResources().getString(R.string.delete_alert),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onExerciseUpdated(Exercise exercise) {
        if (exercise != null)
            fillList(editText.getText().toString());
        else
            Toast.makeText(this, getResources().getString(R.string.update_alert),
                    Toast.LENGTH_LONG).show();
    }

    public void add(View view) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void delete(View view) {
        Intent intent = new Intent(this, DeleteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getResources().getString(R.string.key_search),editText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        editText.setText(savedInstanceState.getString(getResources().getString(R.string.key_search)));
    }
}
