package com.example.brencodie.vertmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.example.brencodie.vertmusic.MusicService.MusicBinder;
import com.example.brencodie.vertmusic.MusicService;
import android.widget.MediaController.MediaPlayerControl;

public class SongActivity extends ActionBarActivity implements MediaPlayerControl{
    private ArrayList<Song> songList;
    private ListView songView;
    MusicService musicSrv;
    private Intent playIntent;
    boolean musicBound = false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private Intent intent;
    private TextView songTitleArtistView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        intent = getIntent();
        getSongs();

        songView = (ListView)findViewById(R.id.song_list);
        songTitleArtistView = (TextView)findViewById(R.id.song_title_artist);
        songList = new ArrayList<>();


        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        controller = new MusicController(this, false);

        setController();
    }

    @Override
    protected void onStart() {
        Log.d("SONGACTIVITYTEST", "onStart");
        super.onStart();
        if(playIntent==null){
            Log.d("PLAYINTENT", "null");


            playIntent = new Intent(getApplicationContext(), MusicService.class);
            boolean check = bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            if (check) {
                Log.d("BINDSERVICE", "true");
            } else {
                Log.d("BINDSERVICE", "false");
            }

            startService(playIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();

        if (musicConnection != null) {
            unbindService(musicConnection);
        }
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Service", "Connected!");
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Service", "Disconnected!");
            musicBound = false;
        }
    };

    public void songPicked(View view){
        Log.d("SONGACTIVITYTEST", "songPicked");

        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        displaySongInfo(true);

        if(playbackPaused){
            Log.d("SONGACTIVITYTEST", "songPicked and playbackPaused");
            setController();
            playbackPaused=false;
        }

        controller.show(0);
    }

    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            Log.d("BROADCASTRECEIVER", "accessed");
            controller.show(0);
        }
    };

    private void displaySongInfo(boolean click) {
        songTitleArtistView.setText("Playing: " + musicSrv.getSongTitle());
        if (click) songTitleArtistView.startAnimation(AnimationUtils.loadAnimation(SongActivity.this, android.R.anim.slide_in_left));
    }

    private void setController(){

        //set the controller up
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SONGACTIVITY", "onClickNext");
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SONGACTIVITY", "onClickPrev");
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_layout));
        controller.setEnabled(true);

    }

    //play next
    private void playNext(){
        Log.d("SONGACTIVITYTEST", "playNext");
        musicSrv.playNext();
        displaySongInfo(true);
        if(playbackPaused){
            Log.d("SONGACTIVITYTEST", "playNext and playbackPaused");
            setController();
            displaySongInfo(true);
            playbackPaused=false;
        }
       controller.show(0);

    }

    //play previous
    private void playPrev(){
        Log.d("SONGACTIVITYTEST", "playPrev");
        musicSrv.playPrev();
        displaySongInfo(true);
        if(playbackPaused){
            Log.d("SONGACTIVITYTEST", "playPrev and playbackPaused");
            setController();
            displaySongInfo(true);
            playbackPaused=false;
        }
       controller.show(0);
    }

    private void getSongs() {
        String url = intent.getStringExtra("songurl");
        final String authorization = intent.getStringExtra("authorization");

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Songs", "Success: " + response.toString());

                        try {
                            JSONArray songListInfo = response.getJSONArray("songs");
                            for (int i = 0; i < songListInfo.length(); i++) {
                                songList.add(new Song(songListInfo.getJSONObject(i).getString("id"), songListInfo.getJSONObject(i).getString("title"), songListInfo.getJSONObject(i).getString("artist") ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.i("Songs", "Error: " + error.toString());
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("authorization", authorization);
                return params;
            }
        };
        queue.add(request);
    }

    @Override
    public void start() {
        Log.d("SONGACTIVITYTEST", "start");
        musicSrv.go();
    }

    @Override
    public void pause() {
        Log.d("SONGACTIVITYTEST", "pause");

        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    protected void onPause(){
        Log.d("SONGACTIVITYTEST", "onPause");

        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        Log.d("SONGACTIVITYTEST", "onResume");

        // Set up receiver for media player onPrepared broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));

        super.onResume();
        if(paused){
            Log.d("SONGACTIVITYTEST", "onResume and paused");

            setController();
            paused=false;
        }

        setController();
    }

    @Override
    protected void onStop() {
        Log.d("SONGACTIVITYTEST", "onStop");
        controller.hide();
        super.onStop();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()) {
           // Log.d("SONGACTIVITYTEST", "getDuration");
            return musicSrv.getDur();
        }
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()) {
          //  Log.d("SONGACTIVITYTEST", "getCurrentPosition");
            return musicSrv.getPosn();
        }
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        Log.d("SONGACTIVITYTEST", "seekTo");
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound) {
           // Log.d("SONGACTIVITYTEST", "isPlaying");
            displaySongInfo(false);
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}