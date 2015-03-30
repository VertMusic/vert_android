package com.example.brencodie.vertmusic;

import android.app.ListActivity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SongActivity extends ListActivity {

    private JSONArray songListInfo;
    private List<Map<String,String>> songs;
    private ArrayList<String> songTitles;
    private ArrayList<String> songArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Intent intent = getIntent();
        getSongs(intent);
        handlePlayer();
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

                        songs = new ArrayList<Map<String,String>>();
                        Map<String,String> songData;

                        try {
                            songListInfo = response.getJSONArray("songs");

                            for (int i = 0; i < songListInfo.length(); i++) {
                                songData = new HashMap<String,String>();
                                songData.put("title", songListInfo.getJSONObject(i).getString("title"));
                                songData.put("artist", songListInfo.getJSONObject(i).getString("artist"));
                                songs.add(songData);
                            }

                           SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), songs, android.R.layout.simple_list_item_2, new String[] {"title", "artist"}, new int[] {android.R.id.text1, android.R.id.text2});
                           getListView().setAdapter(adapter);
                          // handlePlayer();


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

    private void handlePlayer() {
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " is selected. Position number: " + position, Toast.LENGTH_LONG).show();
                String songInfo = parent.getItemAtPosition(position).toString();
                String songTitle = "";
                String songArtist = "";

                for (int i = 0; i < songs.size(); i++) {
                    if (songInfo.equals(songs.get(i).toString())) {
                        songTitle = songs.get(i).get("title");
                        songArtist = songs.get(i).get("artist");
                    }
                }

                for (int i = 0; i < songListInfo.length(); i++) {
                    try {
                        if (checkMatchingSong(songTitle, songArtist, i)) {
                            String url = "http://192.168.56.101:8080/vert/file/song/" + songListInfo.getJSONObject(i).getString("id");

                            Log.i("URL", url);
                            final Intent intent = new Intent(view.getContext(), Player.class);
                            intent.putExtra("songurl", url);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkMatchingSong(String songTitle, String songArtist, int i) throws JSONException {
        return songTitle.equals(songListInfo.getJSONObject(i).getString("title")) && songArtist.equals(songListInfo.getJSONObject(i).getString("artist"));
    }

//    private void handlePlaySong() {
//
//        String url = null; // your URL here
//        try {
//            url = "http://192.168.56.101:8080/vert/file/song/" + songListInfo.getJSONObject(1).getString("id");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.i("URL:" , url);
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        try {
//            mediaPlayer.setDataSource(url);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.i("Error streaming song at:", url);
//        }
//
//    }


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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
