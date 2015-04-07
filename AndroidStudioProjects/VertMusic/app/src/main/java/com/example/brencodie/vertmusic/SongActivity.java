package com.example.brencodie.vertmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);


        Intent intent = getIntent();
        getSongs(intent);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<>();


        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        setController();
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

    @Override
    protected void onStart() {
        Log.d("SONGACTIVITYTEST", "SongActivity onStart");
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

    public void songPicked(View view){
       // Log.i("Clicked:", view.getTag().toString());
        //Log.i("Clicked,", songList.get(Integer.parseInt(view.getTag().toString())).getID());

        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void getSongs(Intent intent) {
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
            case R.id.action_shuffle:
                //shuffle
                musicSrv.setShuffle();
                break;
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

    private void setController(){
        //set the controller up
        controller = new MusicController(this, false);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_layout));
        controller.setEnabled(true);

    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()) {
            return musicSrv.getDur();
        }
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        }
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound) {
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

//public class SongActivity extends ListActivity {
//
//    private JSONArray songListInfo;
//    private List<Map<String,String>> songs;
//    private ArrayList<Song> songList;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_song);
//        Intent intent = getIntent();
//        songList = new ArrayList<>();
//
//        getSongs(intent);
//        handlePlayer();
//    }
//
//
//    private void getSongs(Intent intent) {
//        String url = intent.getStringExtra("songurl");
//        final String authorization = intent.getStringExtra("authorization");
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.i("Songs", "Success: " + response.toString());
//
//                        songs = new ArrayList<>();
//                        Map<String,String> songData;
//
//                        try {
//                            songListInfo = response.getJSONArray("songs");
//
//                            for (int i = 0; i < songListInfo.length(); i++) {
//                                songData = new HashMap<String,String>();
//                                songData.put("title", songListInfo.getJSONObject(i).getString("title"));
//                                songData.put("artist", songListInfo.getJSONObject(i).getString("artist"));
//                                songs.add(songData);
//                                songList.add(new Song(songListInfo.getJSONObject(i).getString("id")));
//                            }
//
//                           SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), songs, android.R.layout.simple_list_item_2, new String[] {"title", "artist"}, new int[] {android.R.id.text1, android.R.id.text2});
//                           getListView().setAdapter(adapter);
//                          // handlePlayer();
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO Auto-generated method stub
//                        Log.i("Songs", "Error: " + error.toString());
//                    }
//                })
//        {
//        @Override
//        public Map<String, String> getHeaders() {
//            Map<String, String> params = new HashMap<String, String>();
//            params.put("authorization", authorization);
//            return params;
//        }
//    };
//
//        queue.add(request);
//    }
//
//    private void handlePlayer() {
//        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//               // Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " is selected. Position number: " + position, Toast.LENGTH_LONG).show();
//                String songInfo = parent.getItemAtPosition(position).toString();
//                String songTitle = "";
//                String songArtist = "";
//
//                for (int i = 0; i < songs.size(); i++) {
//                    if (songInfo.equals(songs.get(i).toString())) {
//                        songTitle = songs.get(i).get("title");
//                        songArtist = songs.get(i).get("artist");
//                    }
//                }
//
//                for (int i = 0; i < songListInfo.length(); i++) {
//                    try {
//                        if (checkMatchingSong(songTitle, songArtist, i)) {
//                            String url = "http://192.168.56.101:8080/vert/file/song/" + songListInfo.getJSONObject(i).getString("id");
//
//                            Log.i("URL", url);
//                            final Intent intent = new Intent(view.getContext(), Player.class);
//                            intent.putExtra("songurl", url);
//                            startActivity(intent);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    private boolean checkMatchingSong(String songTitle, String songArtist, int i) throws JSONException {
//        return songTitle.equals(songListInfo.getJSONObject(i).getString("title")) && songArtist.equals(songListInfo.getJSONObject(i).getString("artist"));
//    }
//
////    private void handlePlaySong() {
////
////        String url = null; // your URL here
////        try {
////            url = "http://192.168.56.101:8080/vert/file/song/" + songListInfo.getJSONObject(1).getString("id");
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////        Log.i("URL:" , url);
////        MediaPlayer mediaPlayer = new MediaPlayer();
////        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
////        try {
////            mediaPlayer.setDataSource(url);
////            mediaPlayer.prepare();
////            mediaPlayer.start();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////            Log.i("Error streaming song at:", url);
////        }
////
////    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_song, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}
