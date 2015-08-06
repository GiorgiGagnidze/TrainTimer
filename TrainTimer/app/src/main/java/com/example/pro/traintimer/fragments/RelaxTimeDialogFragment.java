package com.example.pro.traintimer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;

import java.sql.Time;
import java.util.ArrayList;


public class RelaxTimeDialogFragment extends DialogFragment{
    private int relaxMinutes = 0;
    private int relaxSeconds = 0;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private ArrayList<String> relaxTime;

    public void setRelaxTime(ArrayList<String> relaxTime) {
        this.relaxTime = relaxTime;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.relax_time_dialog);
        Button relaxTimeButton = (Button)d.findViewById(R.id.relax_time_submit_but);
        minutePicker = (NumberPicker)d.findViewById(R.id.minutes);
        minutePicker.setMaxValue(App.MAX_TIME);
        minutePicker.setMinValue(App.MIN_TIME);
        secondPicker = (NumberPicker)d.findViewById(R.id.seconds);
        secondPicker.setMinValue(App.MIN_TIME);
        secondPicker.setMaxValue(App.MAX_TIME);
        if (savedInstanceState != null){
            relaxMinutes = savedInstanceState.getInt(getResources().getString(R.string
                    .key_minutes));
            relaxSeconds = savedInstanceState.getInt(getResources().getString(R.string
                    .key_seconds));
            relaxTime=savedInstanceState.getStringArrayList(getResources().getString(R.string
                    .key_relax_time));
            minutePicker.setValue(relaxMinutes);
            secondPicker.setValue(relaxSeconds);
        }
        final Activity activity = getActivity();
        relaxTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int minutes = minutePicker.getValue();
                int seconds = secondPicker.getValue();
                if (seconds == 0 && minutes == 0)
                    Toast.makeText(activity, activity.getResources().getString(R.string.time_alert),
                            Toast.LENGTH_LONG).show();
                else {
                    relaxTime.add(new Time(0,minutes,seconds).toString());
                    d.dismiss();
                }
            }
        });
        return d;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        relaxMinutes = minutePicker.getValue();
        relaxSeconds = secondPicker.getValue();
        outState.putInt(getResources().getString(R.string.key_minutes),relaxMinutes);
        outState.putInt(getResources().getString(R.string.key_seconds),relaxSeconds);
        outState.putStringArrayList(getResources().getString(R.string.key_relax_time), relaxTime);
    }
}
