package com.example.brencodie.vertmusic;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
    private String playlistList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Intent intent = getIntent();
        getPlaylists(intent);
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

                            ArrayList<String> playlistList = new ArrayList<String>();
                            setListAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, playlistList));


                                JSONObject objects = playlistInfo.getJSONObject(0);
                                JSONObject objects2 = playlistInfo.getJSONObject(1);

                            TextView testText = (TextView) findViewById(R.id.testText);
                            testText.setText(objects.getString("name"));


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
