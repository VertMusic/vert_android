package com.example.brencodie.vertmusic;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LogIn extends ActionBarActivity {

    public final static String ACCESS_TOKEN = "com.example.brencodie.ACCESS";
    public final static String USER_ID = "com.example.brencodie.ID";

    private TextView errorText;
    private String accessTokenValue;
    private String userIdValue;
    private JSONObject logInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_log_in);
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onSignInClick(View view) {
        final Intent intent = new Intent(this, PlaylistActivity.class);
        errorText = (TextView) findViewById(R.id.errorText); // Changes text based on server response
        EditText user = (EditText) findViewById(R.id.email); // Assign variable user to the e-mail id
        EditText pass = (EditText) findViewById(R.id.password); // Assign variable pass to the password id
        String usernameValue = user.getText().toString(); // Convert user text field into string and assign to usernameValue
        String passwordValue = pass.getText().toString(); // Convert pass text field into string and assign to passwordValue

        RequestQueue queue = Volley.newRequestQueue(this); // Creates a request of this instance?

        ///{"session":{"username":"dev","password":"password"}}
        Map sessionInfo = new HashMap();
        sessionInfo.put("username", usernameValue);
        sessionInfo.put("password", passwordValue);

        final Map requestObject = new HashMap();
        requestObject.put("session", sessionInfo);

        JSONObject json = new JSONObject(requestObject);
        String url = "http://192.168.56.101:8080/vert/data/session";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Login", "Success: " + response.toString());
                        ///Look in Java DataController in vert_backend for response type
                        ///TODO: transition to new activity where user can see their playlists

                        try {
                            logInfo = response.getJSONObject("session"); // Acquire the JSON object containing authentication token and userId
                            accessTokenValue = logInfo.getString("accessToken"); // Save the authentication token string
                            userIdValue = logInfo.getString("userId"); // Save the userId string
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        intent.putExtra(ACCESS_TOKEN, accessTokenValue);
                        intent.putExtra(USER_ID, userIdValue);
                        startActivity(intent);

                        /// "authorization": response.get("authToken");
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Login", "Error: " + error.toString());
                        ///TODO: let user know login failed
                        errorText.setText("That didn't work!");
                    }
                });

        queue.add(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_in, menu);
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