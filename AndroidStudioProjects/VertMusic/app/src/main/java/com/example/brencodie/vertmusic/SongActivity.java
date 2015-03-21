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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SongActivity extends ListActivity {

    private JSONArray songListInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Intent intent = getIntent();
        getSongs(intent);
    }

    private void getSongs(Intent intent) {
        String url = intent.getStringExtra("songurl");

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Songs", "Success: " + response.toString());

                        List<Map<String,String>> songs = new ArrayList<Map<String,String>>();


                        try {
                            songListInfo = response.getJSONArray("songs");

                            for (int i = 0; i < songListInfo.length(); i++) {
                                Map<String,String> songData = new HashMap<String,String>();
                                songData.put("title", songListInfo.getJSONObject(i).getString("title"));
                                songData.put("artist", songListInfo.getJSONObject(i).getString("artist"));
                                songs.add(songData);
                            }

                           SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), songs, android.R.layout.simple_list_item_2, new String[] {"title", "artist"}, new int[] {android.R.id.text1, android.R.id.text2});
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
                        Log.i("Songs", "Error: " + error.toString());
                    }
                });


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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
