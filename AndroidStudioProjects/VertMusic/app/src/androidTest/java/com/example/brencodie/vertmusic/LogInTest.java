package com.example.brencodie.vertmusic;

import android.app.Application;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ApplicationTestCase;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Created by brenbln on 3/24/15.
 */
public class LogInTest extends ActivityInstrumentationTestCase2<LogIn> {

    Button signInButton;
    Button registerButton;
    LogIn activity;
    public final static String ACCESS_TOKEN = "com.example.brencodie.ACCESS";
    public final static String USER_ID = "com.example.brencodie.ID";
    private String accessTokenValue;
    private String userIdValue;
    private JSONObject logInfo;

    public LogInTest() {
        super(LogIn.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    /*
    http://stackoverflow.com/questions/23060921/android-test-activityinstrumentationtestcase2-test-2-buttons-where-each-one
     */
//    public void testRegistrationButton() {
//
//        assertNotNull(activity);
//        registerButton = (Button) activity.findViewById(R.id.user_sign_in_button);
//
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                registerButton.performClick();
//            }
//        });
//
//        getInstrumentation().waitForIdleSync();
//    }

//    public void testSignInButton () {
//
//        final Intent intent = new Intent(activity, PlaylistActivity.class);
//        signInButton = (Button) activity.findViewById(R.id.user_sign_in_button);
//        EditText user = (EditText) activity.findViewById(R.id.email); // Assign variable user to the e-mail id
//        EditText pass = (EditText) activity.findViewById(R.id.password); // Assign variable pass to the password id
//
//        // assertEquals("", user.getText().toString());
//        // assertNull("Username field:", user);
//
//        RequestQueue queue = Volley.newRequestQueue(activity); // Creates a request of this instance?
//
//        ///{"session":{"username":"dev","password":"password"}}
//        Map sessionInfo = new HashMap();
//        sessionInfo.put("username", "brenbln");
//        sessionInfo.put("password", "password");
//
//        final Map requestObject = new HashMap();
//        requestObject.put("session", sessionInfo);
//
//        JSONObject json = new JSONObject(requestObject);
//        String url = "http://192.168.56.101:8080/vert/data/session";
//
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                signInButton.performClick();
//            }
//        });
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.i("Login", "Success: " + response.toString());
//                        ///Look in Java DataController in vert_backend for response type
//                        ///TODO: transition to new activity where user can see their playlists
//
//                        try {
//                            logInfo = response.getJSONObject("session"); // Acquire the JSON object containing authentication token and userId
//                            accessTokenValue = logInfo.getString("accessToken"); // Save the authentication token string
//                            userIdValue = logInfo.getString("userId"); // Save the userId string
//
//                            intent.putExtra(ACCESS_TOKEN, accessTokenValue);
//                            intent.putExtra(USER_ID, userIdValue);
//                            activity.startActivity(intent);
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//
//
//                        /// "authorization": response.get("authToken");
//                    }
//                },
//                new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("Login", "Error: " + error.toString());
//                        ///TODO: let user know login failed
//                        //Toast.makeText(activity.getBaseContext(), "Invalid information entered.", Toast.LENGTH_LONG).show();
//
//                    }
//                });
//        queue.add(request);
//        assertNull(logInfo);
//    }




}
