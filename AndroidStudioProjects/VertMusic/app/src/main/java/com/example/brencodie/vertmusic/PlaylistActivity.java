package com.example.brencodie.vertmusic;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PlaylistActivity extends ListActivity {

    private String accessToken;
    private String userId;
    private JSONArray playlistInfo;
    private ArrayList<String> songIdList;
    private ArrayList<String> playlistList;
    private ArrayAdapter<String> adapter;
    //public final static String SONG_URL = "com.example.brencodie.songurl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Intent intent = getIntent();
        getPlaylists(intent);

        handleSongURL();


    }

    private void getPlaylists(Intent intent) {
        accessToken = intent.getStringExtra(LogIn.ACCESS_TOKEN);
        userId = intent.getStringExtra(LogIn.USER_ID);

        String url = "http://192.168.56.101:8080/vert/data/playlists";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Playlist", "Success: " + response.toString());

                        try {
                            playlistInfo = response.getJSONArray("playlists");

                            playlistList = new ArrayList();

                            for (int i = 0; i < playlistInfo.length(); i++) {
                                String playlistName = playlistInfo.getJSONObject(i).getString("name");
                                playlistList.add(playlistName);
                                // Log.i("Name of playlist:", playlistName);
                            }

                            adapter = new ArrayAdapter(getListView().getContext(), android.R.layout.simple_list_item_1, playlistList);
                            getListView().setAdapter(adapter);



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.i("Playlist", "Error: " + error.toString());
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("authorization", accessToken);
                return params;
            }
        };
        queue.add(request);
    }

    private void handleSongURL() {
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " is selected. Position number: " + position, Toast.LENGTH_LONG).show();
                String playlistName = parent.getItemAtPosition(position).toString();
                songIdList = new ArrayList();

                for (int i = 0; i < playlistInfo.length(); i++) {
                    try {
                        if (playlistName.equals(playlistInfo.getJSONObject(i).getString("name"))) {

                            if (playlistInfo.getJSONObject(i).getJSONArray("songs").length() >= 1) {
                                try {
                                    for (int j = 0; j < playlistInfo.getJSONObject(i).getJSONArray("songs").length(); j++) {
                                        songIdList.add(playlistInfo.getJSONObject(i).getJSONArray("songs").getString(j));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String url = "http://192.168.56.101:8080/vert/data/songs?";
                                for (String songId : songIdList) {
                                    if (songIdList.indexOf(songId) != 0) {
                                        url += "&";
                                    }

                                    String song = "ids[]=" + songId;
                                    url += song;
                                }

                                final Intent intent = new Intent(view.getContext(), SongActivity.class);
                                intent.putExtra("songurl", url);
                                startActivity(intent);
                            }



                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
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
