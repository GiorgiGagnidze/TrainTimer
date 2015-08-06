package com.example.pro.traintimer.model;

public class PlayList {
    private int ID;
    private String name;
    private int musicIndex;
    private int start;

    public PlayList(String name,int musicIndex,int start){
        this.name = name;
        this.musicIndex = musicIndex;
        this.start = start;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public int getMusicIndex() {
        return musicIndex;
    }

    public int getStart() {
        return start;
    }

    public String getName() {
        return name;
    }

    public void setMusicIndex(int musicIndex) {
        this.musicIndex = musicIndex;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
