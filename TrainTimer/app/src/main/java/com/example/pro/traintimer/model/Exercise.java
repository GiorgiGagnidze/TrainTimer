package com.example.pro.traintimer.model;

import java.util.ArrayList;

public class Exercise {
    private String exerciseName;
    private String playlistName;
    private String songName;
    private String info;
    private int musicOption;
    private String relaxTime;
    private ArrayList<String> sets;
    private String ID;

    public Exercise(String exerciseName,String playlistName,String songName,String info,int musicOption,
                    String relaxTime,ArrayList<String> sets){
        this.exerciseName = exerciseName;
        this.playlistName = playlistName;
        this.songName = songName;
        this.info = info;
        this.musicOption = musicOption;
        this.relaxTime = relaxTime;
        this.sets = sets;
    }

    public ArrayList<String> getSets() {
        return sets;
    }

    public int getMusicOption() {
        return musicOption;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getInfo() {
        return info;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public String getRelaxTime() {
        return relaxTime;
    }

    public String getSongName() {
        return songName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
