package com.thirteen_lab.wifi_searcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thirteen_lab.wifi_searcher.config.Constants;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private LinearLayout listWifi;
    private ImageView settingsBtn;
    private SimpleDateFormat oldFormat;
    private SimpleDateFormat newFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("USER_AUTH", MODE_PRIVATE);

        ((TextView) findViewById(R.id.userUsername)).setText(sharedPreferences.getString("username", null));
        ((TextView) findViewById(R.id.userLogin)).setText("ID: " + sharedPreferences.getString("login", null));
        listWifi = findViewById(R.id.historyList);
        settingsBtn = findViewById(R.id.settingsBtnView);
        oldFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        newFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        int id = sharedPreferences.getInt("id", 0);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getUserWifi = new JsonArrayRequest(Request.Method.GET,
                Constants.Base_URL + "/wifi/getUserWifi/" + id, null,
                new Response.Listener<JSONArray>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONArray resArr) {
                        HorizontalScrollView history;
                        TextView wifiAddress;
                        TextView wifiName;
                        TextView wifiDistance;
                        TextView wifiHz;
                        TextView wifiChecked;
                        TextView wifiLevel;

                        LayoutInflater layoutInflater = getLayoutInflater();
                        listWifi.removeAllViews();

                        for (int i = 0; i < resArr.length(); i++) {
                            try {
                                // TODO: ДАННЫЕ КОТОРЫЕ ВЫДАЕТ: id, name, bssid, distance, level, security, frequency, lat, lng, accuracy, city, zipcode, streetName, streetNumber, countryCode, created_at, updated_at

                                JSONObject res = resArr.getJSONObject(i);

                                history = ((HorizontalScrollView) layoutInflater.inflate(R.layout.profile_actions, listWifi, false));

                                wifiAddress = history.findViewById(R.id.wifiAddress);
                                wifiName = history.findViewById(R.id.wifiName);
                                wifiDistance = history.findViewById(R.id.wifiDistance);
                                wifiHz = history.findViewById(R.id.wifiHz);
                                wifiChecked = history.findViewById(R.id.wifiChecked);
                                wifiLevel = history.findViewById(R.id.wifiLev);

                                wifiAddress.setText(res.getString("countryCode") + ", " + res.getString("city")
                                        + ", " + res.getString("streetName"));
                                wifiName.setText(res.getString("name"));
                                wifiDistance.setText(res.getString("distance") + "m");
                                wifiHz.setText(res.getString("frequency") + "MHz");

                                String date = res.getString("created_at");
                                Date oldDate = oldFormat.parse(date.replace('T', ' '));
                                String newDate = newFormat.format(oldDate);

                                wifiChecked.setText(newDate);
                                wifiLevel.setText(res.getString("level") + "dB");

                                listWifi.addView(history);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "An error has occurred! Try again", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + sharedPreferences.getString("jwt", null));
                return params;
            }
        };

        queue.add(getUserWifi);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.accessPoints:
                        startActivity(new Intent(getApplicationContext(), AccessPointsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        return true;

                    case R.id.heatMapWifi:
                        startActivity(new Intent(getApplicationContext(), HeatMapActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });
    }
}