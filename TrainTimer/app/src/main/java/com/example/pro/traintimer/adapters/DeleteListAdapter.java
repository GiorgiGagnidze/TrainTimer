package com.example.pro.traintimer.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.pro.traintimer.R;
import com.example.pro.traintimer.model.Entry;
import com.example.pro.traintimer.model.Exercise;

import java.util.ArrayList;

public class DeleteListAdapter extends BaseAdapter{
    private ArrayList<Entry> entries;
    private Context context;

    public DeleteListAdapter(ArrayList<Entry> entries,Context context){
        this.entries = entries;
        this.context = context;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public ArrayList<Exercise> getCheckedExercises(){
        ArrayList<Exercise> exercises = new ArrayList<>();
        for (Entry e: entries){
            if (e.isChecked())
                exercises.add(e.getExercise());
        }
        return exercises;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int i) {
        return entries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = View.inflate(context, R.layout.list_item, null);
            Holder h = new Holder();
            h.txtV = (TextView)view.findViewById(R.id.item_text);
            h.checkBox = (CheckBox)view.findViewById(R.id.item_checkBox);
            view.setTag(h);
        }
        Holder h = (Holder)view.getTag();

        final Entry entry = entries.get(i);
        h.txtV.setText(entry.getExercise().getExerciseName());
        h.checkBox.setChecked(entry.isChecked());
        h.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                entry.setChecked(b);
            }
        });
        return view;
    }

    public static class Holder {
        TextView txtV;
        CheckBox checkBox;
    }
}
