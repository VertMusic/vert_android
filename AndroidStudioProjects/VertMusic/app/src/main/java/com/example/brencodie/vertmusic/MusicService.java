package com.example.brencodie.vertmusic;

import android.annotation.TargetApi;
import android.app.Service;
import java.util.ArrayList;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.widget.TextView;

/**
 * Created by Bren Codie on 4/5/2015.
 * Based off of code from the following tutorial:
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;
    private TextView songTitleArtist;

    public void onCreate() {
        Log.d("SERVICETEST", "MusicService onCreate");
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        rand=new Random();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        Log.d("SERVICETEST", "MusicService initMusicPlayer");
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        Log.d("SERVICETEST", "List set");
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            Log.d("SERVICETEST", "Returning service");
            return MusicService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICETEST", "MusicService onBind");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("SERVICETEST", "MusicService onUnbind");
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        //play a song
        player.reset();

        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        //get id
        String currSong = playSong.getID();
        //set URL of song
        String trackURL = "http://192.168.56.101:8080/vert/file/song/" + currSong;
        Log.i("URL:", trackURL);

        try{
            player.setDataSource(trackURL);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public String getSongTitle() {
        return songs.get(songPosn).getTitle();
    }

    public String getSongArtist() {
        return songs.get(songPosn).getArtist();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        Log.d("MUSICSERVICETEST", "onPrepared");
        mp.start();
        Intent notIntent = new Intent(this, SongActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

       Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
       LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);


        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn >= songs.size()) songPosn=0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
