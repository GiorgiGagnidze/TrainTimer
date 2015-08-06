package com.example.pro.traintimer.transport;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.pro.traintimer.App;
import com.example.pro.traintimer.R;
import com.example.pro.traintimer.db.MyDBHelper;
import com.example.pro.traintimer.model.Exercise;
import com.example.pro.traintimer.model.PlayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class TimeScheduler {
    private Thread thread;
    private Context context;
    private ExerciseStateListener listener;
    private ContentResolver resolver;
    private int index;
    private MediaPlayer playlistPlayer;
    private CountDownLatch startSignal;
    private App app;

    public TimeScheduler(Context context,ExerciseStateListener listener,ContentResolver resolver,App app){
        this.context=context;
        this.listener=listener;
        this.resolver = resolver;
        this.app = app;
    }

    public void start(final Exercise exercise){
        String relax = exercise.getRelaxTime();
        final int musicOption = exercise.getMusicOption();
        final long relaxTimeMillis = Integer.parseInt(relax.substring(3,5))*60000+
                Integer.parseInt(relax.substring(6))*1000;

        switch (musicOption){
            case App.SONG:
                executeWithSong(relaxTimeMillis,exercise);
                break;
            case App.PLAYLIST:
                executeWithPlaylist(relaxTimeMillis,exercise);
                break;
            case App.NO_MUSIC:
                executeWithNoMusic(relaxTimeMillis,exercise);
                break;
        }
    }

    private void executeWithSong(final long relaxTimeMillis,final Exercise exercise){
        final MediaPlayer songPlayer = new MediaPlayer();
        String songPath;
        songPath = getSongPath(DatabaseUtils.sqlEscapeString(exercise.getSongName()));
        if (songPath != null) {
            try {
                songPlayer.setDataSource(songPath);
                songPlayer.prepare();
                songPlayer.setLooping(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final String path = songPath;

        if (path == null){
            executeWithNoMusic(relaxTimeMillis,exercise);
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> array = exercise.getSets();
                int i=0;
                for (String set: array){
                    long setMillis = Integer.parseInt(set.substring(0,2))*60000+
                            Integer.parseInt(set.substring(3))*1000;

                    songPlayer.start();
                    try {
                        Thread.sleep(setMillis);
                    } catch (InterruptedException e) {
                        songPlayer.stop();
                        break;
                    }

                    songPlayer.pause();
                    if (i==array.size()-1){
                        listener.onExerciseEnded();
                        break;
                    }
                    try {
                        Thread.sleep(relaxTimeMillis);
                    } catch (InterruptedException e) {
                        break;
                    }
                    i++;
                }
            }
        });
        thread.start();
    }

    private void executeWithNoMusic(final long relaxTimeMillis,final Exercise exercise){
        final MediaPlayer noMusicPlayer = MediaPlayer.create(context, R.raw.signal);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> array = exercise.getSets();
                int i=0;
                for (String set: array){
                    long setMillis = Integer.parseInt(set.substring(0,2))*60000+
                            Integer.parseInt(set.substring(3))*1000;

                    noMusicPlayer.start();
                    try {
                        Thread.sleep(setMillis);
                    } catch (InterruptedException e) {
                        break;
                    }

                    noMusicPlayer.start();
                    if (i==array.size()-1){
                        listener.onExerciseEnded();
                        break;
                    }
                    try {
                        Thread.sleep(relaxTimeMillis);
                    } catch (InterruptedException e) {
                        break;
                    }
                    i++;
                }
            }
        });
        thread.start();
    }

    private void executeWithPlaylist(final long relaxTimeMillis,final Exercise exercise){
        final Cursor cursor = getPlaylist(getPlaylistID(exercise.getPlaylistName()));
        if (cursor == null){
            executeWithNoMusic(relaxTimeMillis, exercise);
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MyDBHelper dbHelper = app.getMyDBHelper();
                PlayList playList = dbHelper.getPlaylist(exercise.getPlaylistName());
                index = 0;
                int start = 0;
                if (playList != null){
                    index = playList.getMusicIndex();
                    start = playList.getStart();
                }
                initPlaylistPlayer(cursor, start, false);
                ArrayList<String> array = exercise.getSets();
                int i=0;
                for (String set: array){
                    long setMillis = Integer.parseInt(set.substring(0,2))*60000+
                            Integer.parseInt(set.substring(3))*1000;

                    playlistPlayer.start();
                    try {
                        Thread.sleep(setMillis);
                    } catch (InterruptedException e) {
                        playlistPlayer.pause();
                        changeTable(dbHelper,exercise,playList);
                        break;
                    }

                    playlistPlayer.pause();
                    if (i==array.size()-1){
                        listener.onExerciseEnded();
                        changeTable(dbHelper,exercise,playList);
                        break;
                    }
                    try {
                        Thread.sleep(relaxTimeMillis);
                    } catch (InterruptedException e) {
                        changeTable(dbHelper,exercise,playList);
                        break;
                    }
                    i++;
                }
            }
        });
        thread.start();
    }

    private void changeTable(MyDBHelper dbHelper,Exercise exercise,PlayList playList){
        if (playList==null) {
            dbHelper.putNewPlaylist(new PlayList(exercise.getPlaylistName(),
                    index - 1, playlistPlayer.getCurrentPosition()));
        }else {
            playList.setMusicIndex(index - 1);
            playList.setStart(playlistPlayer.getCurrentPosition());
            dbHelper.updatePlaylist(playList);
        }
    }

    private void initPlaylistPlayer(final Cursor cursor, int start, boolean toStart){
        playlistPlayer = new MediaPlayer();
        playlistPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                initPlaylistPlayer(cursor,0,true);
            }
        });

        if (index == cursor.getCount())
            index = 0;
        final String column = MediaStore.Audio.Media.DATA;
        cursor.moveToPosition(index);
        String path = cursor.getString(cursor.getColumnIndex(column));
        try {
            playlistPlayer.setDataSource(path);
            playlistPlayer.prepare();
            playlistPlayer.setLooping(false);
            if (start != 0) {
                startSignal = new CountDownLatch(1);
                playlistPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        startSignal.countDown();
                    }
                });
                playlistPlayer.seekTo(start);
                try {
                    startSignal.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (toStart)
            playlistPlayer.start();
        index++;
    }

    private Cursor getPlaylist(long ID){
        if (ID == -1)
            return null;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", ID);
        String dataKey = MediaStore.Audio.Media.DATA;
        Cursor tracks = resolver.query(uri, new String[] { dataKey }, null, null, null);
        return tracks;
    }

    private long getPlaylistID(String playlistName){
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String idKey = MediaStore.Audio.Playlists._ID;
        String nameKey = MediaStore.Audio.Playlists.NAME;
        String[] columns = { idKey, nameKey };
        String selection = nameKey +" = "+DatabaseUtils.sqlEscapeString(playlistName);

        Cursor playList = resolver.query(uri, columns, selection, null, null);
        long ID = -1;
        if (playList != null){
            if (playList.moveToFirst())
                ID = playList.getLong(playList.getColumnIndex(idKey));
            playList.close();
        }
        return ID;
    }

    private String getSongPath(String songName){
        String[] all = { "*" };
        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND "+
        MediaStore.Audio.Media.TITLE +" = "+songName+"";
        String path=null;

        Cursor cursor = resolver.query(allSongsUri,all,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));
            cursor.close();
        }
        return path;
    }

    public void stop(){
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
