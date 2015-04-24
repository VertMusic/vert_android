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
 * Used the following tutorial to build the actual media player:
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764
 * Tutorial assumes that you are playing songs from phone; modified to play songs from a stream instead. Also added
 * a broadcast manager and disabled a few features/fixed some issues in the original tutorial.
 *
 * A class that extends Service will run in the background. This class will run the media player in the background, letting
 * songs play in the background.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;

    public void onCreate() {
        Log.d("SERVICETEST", "MusicService onCreate");
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        rand=new Random();
        initMusicPlayer();
    }

    /**
     * Sets the media player up.
     */
    public void initMusicPlayer() {
        Log.d("SERVICETEST", "MusicService initMusicPlayer");
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    /**
     * Sets the list of songs to use.
     * @param theSongs
     */
    public void setList(ArrayList<Song> theSongs) {
        Log.d("SERVICETEST", "List set");
        songs = theSongs;
    }

    /**
     * Uses in SongActivity.class to connect or bind to this service.
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            Log.d("SERVICETEST", "Returning service");
            return MusicService.this;
        }
    }

    /**
     * Checks whether the service has been bound
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICETEST", "MusicService onBind");
        return musicBind;
    }

    /**
     * Handles cases in which the service is disconnected; the player is stopped
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent){
        Log.d("SERVICETEST", "MusicService onUnbind");
        player.stop();
        player.release();
        return false;
    }

    /**
     * Takes the URL from a song and plays it
     */
    public void playSong(){
        //play a song
        player.reset();

        //get song
        Song playSong = songs.get(songPosn);
        //get id
        String currSong = playSong.getID();
        //set URL of song
        String trackURL = Constants.IP_ADDRESS + "/vert/file/song/" + currSong;
        Log.i("URL:", trackURL);

        try{
            player.setDataSource(trackURL);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    /**
     * Sets the position of the song
     * @param songIndex
     */
    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    /**
     * Returns title of current song playing
     * @return
     */
    public String getSongTitle() {
        return songs.get(songPosn).getTitle();
    }

    /**
     * Returns title of artist of song playing
     * @return
     */
    public String getSongArtist() {
        return songs.get(songPosn).getArtist();
    }

    /**
     * Runs when a song completes on its own
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    /**
     * Handles errors with the media player
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    /**
     * Runs whenever the media player plays a song for the first time. Contains a broadcast manager to update the player automatically.
     * @param mp
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) // Not one hundred percent sure what this part does, but assuming it targets a specific Android version
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        Log.d("MUSICSERVICETEST", "onPrepared");
        mp.start();
        Intent notIntent = new Intent(this, SongActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
//               notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

       Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
       LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

//        builder.setContentIntent(pendInt)
//                .setSmallIcon(R.drawable.play)
//                .setTicker(songTitle)
//                .setOngoing(true)
//                .setContentTitle("Playing")
//        .setContentText(songTitle);
//        Notification not = builder.build();
//
//        startForeground(NOTIFY_ID, not);
    }

    /**
     * Returns the player's current position
     * @return
     */
    public int getPosn(){
        return player.getCurrentPosition();
    }

    /**
     * Returns the duration of the song playing
     * @return
     */
    public int getDur(){
        return player.getDuration();
    }

    /**
     * Returns true if a song is playing
     * @return
     */
    public boolean isPng(){
        return player.isPlaying();
    }

    /**
     * Pauses the player when called
     */
    public void pausePlayer(){
        player.pause();
    }

    /**
     * Handles seeking within a track
     * @param posn
     */
    public void seek(int posn){
        player.seekTo(posn);
    }

    /**
     * Starts the player when called
     */
    public void go(){
        player.start();
    }

    /**
     * Handles the functionality for clicking on the Prev button
     */
    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }

    /**
     * Handles functionality for clicking on the Next button
     */
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
