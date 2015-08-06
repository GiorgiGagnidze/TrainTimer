package com.example.pro.traintimer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;
import java.sql.Time;
import java.util.ArrayList;


public class SetDialogFragment extends DialogFragment{
    private boolean isChronometerStarted = false;
    private ArrayList<String> sets;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private int relaxMinutes = 0;
    private int relaxSeconds = 0;
    private long stopingTime = 0;
    private long chronometerBase = 0;
    private int index = -1;

    public void setSets(ArrayList<String> sets) {
        this.sets = sets;
    }

    public void setIndex(int index){
        this.index = index;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.set_dialog);
        Button start = (Button)d.findViewById(R.id.start_but);
        Button stop = (Button)d.findViewById(R.id.stop_but);
        Button setSubmitButton = (Button)d.findViewById(R.id.set_submit_but);
        minutePicker = (NumberPicker)d.findViewById(R.id.minutes1);
        minutePicker.setMaxValue(App.MAX_TIME);
        minutePicker.setMinValue(App.MIN_TIME);
        secondPicker = (NumberPicker)d.findViewById(R.id.seconds1);
        secondPicker.setMinValue(App.MIN_TIME);
        secondPicker.setMaxValue(App.MAX_TIME);
        final Activity activity = getActivity();
        final Chronometer chronometer = (Chronometer)d.findViewById(R.id.chronometer);
        if (savedInstanceState != null) {
            sets = savedInstanceState.getStringArrayList(getResources().getString(R.string.key_sets));
            relaxMinutes = savedInstanceState.getInt(getResources().getString(R.string
                    .key_minutes));
            relaxSeconds = savedInstanceState.getInt(getResources().getString(R.string
                    .key_seconds));
            stopingTime = savedInstanceState.getLong(getResources().getString(R.string
                    .key_chronometer_stop));
            isChronometerStarted = savedInstanceState.getBoolean(getResources().getString(R.string
                    .key_chronometer_state));
            chronometerBase = savedInstanceState.getLong(getResources().getString(R.string
                    .key_chronometer_base));
            index = savedInstanceState.getInt(getResources().getString(R.string
                    .key_index));
            minutePicker.setValue(relaxMinutes);
            secondPicker.setValue(relaxSeconds);
            if (isChronometerStarted){
                chronometer.setBase(chronometerBase-1000);
                chronometer.start();
            } else if (chronometerBase != 0)
                chronometer.setBase(chronometerBase+SystemClock.elapsedRealtime()-stopingTime);
        }
        final RadioGroup radioGroup = (RadioGroup)d.findViewById(R.id.input_type);
        radioGroup.check(R.id.radio_picker);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChronometerStarted) {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    chronometerBase = chronometer.getBase();
                    isChronometerStarted = true;
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChronometerStarted ) {
                    isChronometerStarted = false;
                    chronometer.stop();
                    stopingTime = SystemClock.elapsedRealtime();
                }
            }
        });
        setSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_chrono){
                    if (isChronometerStarted ) {
                        String toAdd = new Time(SystemClock.elapsedRealtime() - chronometerBase).toString().
                                substring(3);
                        if (index == -1)
                            sets.add(toAdd);
                        else
                            sets.set(index,toAdd);
                        App.getSetsAdapter().notifyDataSetChanged();
                        d.dismiss();
                    } else if (stopingTime != 0){
                        String toAdd = new Time(stopingTime - chronometerBase).toString().
                                substring(3);
                        if (index == -1)
                            sets.add(toAdd);
                        else
                            sets.set(index,toAdd);
                        App.getSetsAdapter().notifyDataSetChanged();
                        d.dismiss();
                    } else
                        Toast.makeText(activity, activity.getResources().getString(R.string.start_alert),
                                Toast.LENGTH_LONG).show();
                } else {
                    int minutes = minutePicker.getValue();
                    int seconds = secondPicker.getValue();
                    if (seconds == 0 && minutes == 0)
                        Toast.makeText(activity,activity.getResources().getString(R.string.time_alert),
                                Toast.LENGTH_LONG).show();
                    else{
                        Time time = new Time(0,minutes,seconds);
                        String toAdd =time.toString().substring(3);
                        if (index == -1)
                            sets.add(toAdd);
                        else
                            sets.set(index,toAdd);
                        App.getSetsAdapter().notifyDataSetChanged();
                        d.dismiss(); 
                    }
                }
            }
        });
        return d;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(getResources().getString(R.string.key_sets), sets);
        relaxMinutes = minutePicker.getValue();
        relaxSeconds = secondPicker.getValue();
        outState.putInt(getResources().getString(R.string.key_minutes),relaxMinutes);
        outState.putInt(getResources().getString(R.string.key_seconds),relaxSeconds);
        outState.putLong(getResources().getString(R.string.key_chronometer_stop),stopingTime);
        outState.putBoolean(getResources().getString(R.string.key_chronometer_state),isChronometerStarted);
        outState.putLong(getResources().getString(R.string.key_chronometer_base),chronometerBase);
        outState.putInt(getResources().getString(R.string.key_index),index);
    }
}
