package com.example.pro.traintimer.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pro.traintimer.R;
import com.example.pro.traintimer.model.PlayList;

public class MyDBHelper extends SQLiteOpenHelper{
    private String tableName;
    private SQLiteDatabase db;
    private String nameCol;
    private String idCol;
    private String musicIndexCol;
    private String startTimeCol;

    public  MyDBHelper(Context context,int version){
        this(context,context.getResources().getString(R.string.dbName),version);
    }

    public MyDBHelper(Context context, String dbName, int version) {
        super(context, dbName, null, version);
        tableName = context.getResources().getString(R.string.playlist_table);
        idCol = context.getResources().getString(R.string.id_column);
        nameCol = context.getResources().getString(R.string.name_column);
        musicIndexCol = context.getResources().getString(R.string.index_column);
        startTimeCol = context.getResources().getString(R.string.start_column);
        db = getWritableDatabase();
    }

    public void putNewPlaylist(PlayList playList){
        ContentValues cv = new ContentValues();
        cv.put(nameCol,playList.getName());
        cv.put(musicIndexCol,playList.getMusicIndex());
        cv.put(startTimeCol,playList.getStart());
        db.insert(tableName, null, cv);
    }

    public void updatePlaylist(PlayList playList){
        ContentValues cv = new ContentValues();
        cv.put(musicIndexCol,playList.getMusicIndex());
        cv.put(startTimeCol,playList.getStart());
        db.update(tableName, cv, idCol + " = " + playList.getID(), null);
    }

    public PlayList getPlaylist(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM "+tableName+ " WHERE "+nameCol+"=?",
                new String[]{name});

        if (cursor!= null && cursor.moveToFirst()) {
            PlayList playList = new PlayList(cursor.getString(cursor.getColumnIndex(nameCol)),
            cursor.getInt(cursor.getColumnIndex(musicIndexCol)),
            cursor.getInt(cursor.getColumnIndex(startTimeCol)));
            playList.setID(cursor.getInt(cursor.getColumnIndex(idCol)));
            cursor.close();
            return playList;
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_PLAYLISTS_TABLE = "CREATE TABLE " +tableName+ "("
                + idCol + " INTEGER PRIMARY KEY, "
                + nameCol + " TEXT, "
                + musicIndexCol + " INTEGER, "
                + startTimeCol + " INTEGER"+")";
        sqLiteDatabase.execSQL(CREATE_PLAYLISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        if (i2 == 1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
            onCreate(db);
        }
    }
}
