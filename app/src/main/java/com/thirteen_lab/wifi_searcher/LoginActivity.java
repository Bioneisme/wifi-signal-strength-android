package com.thirteen_lab.wifi_searcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.textfield.TextInputEditText;
import com.thirteen_lab.wifi_searcher.config.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextInputEditText txtLogin, txtPassword;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        RequestQueue queue = Volley.newRequestQueue(this);

        sharedPreferences = getSharedPreferences("USER_AUTH", MODE_PRIVATE);

        txtLogin = findViewById(R.id.editTextLogin);
        txtPassword = findViewById(R.id.editTextPass);

        loginBtn = (Button) findViewById(R.id.loginBtn);

        validateLogin();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> paramsLogin = new HashMap<>();
                paramsLogin.put("login", Objects.requireNonNull(txtLogin.getText()).toString());
                paramsLogin.put("password", Objects.requireNonNull(txtPassword.getText()).toString());

                JSONObject objectLogin = new JSONObject(paramsLogin);
                JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.Base_URL + "/users/login",
                        objectLogin, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String jwt = response.getString("token");
                            String username = response.getString("username");
                            String login = response.getString("login");
                            int id = response.getInt("id");

                            rememberLogin(jwt, username, login, id);

                            loginBtn.setVisibility(View.VISIBLE);

                            Intent intent = new Intent(getBaseContext(), AccessPointsActivity.class);
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        loginBtn.setVisibility(View.VISIBLE);
                    }
                });

                loginBtn.setVisibility(View.INVISIBLE);

                queue.add(loginRequest);
            }
        });


    }

    private void rememberLogin(String jwt, String username, String login, int id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt", jwt);
        editor.putString("username", username);
        editor.putString("login", login);
        editor.putInt("id", id);
        editor.apply();
    }

    private void validateLogin() {
        String jwt = sharedPreferences.getString("jwt", null);
        String login = sharedPreferences.getString("login", null);

        if (jwt != null) {
            txtLogin.setText(login);

            RequestQueue queue = Volley.newRequestQueue(this);

            loginBtn.setVisibility(View.INVISIBLE);

            Map<String, String> paramsLogin = new HashMap<>();
            paramsLogin.put("token", jwt);

            JSONObject objectLogin = new JSONObject(paramsLogin);

            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, Constants.Base_URL + "/users/validate",
                    objectLogin, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String jwt = response.getString("token");
                        String username = response.getString("username");
                        String login = response.getString("login");
                        int id = response.getInt("id");

                        rememberLogin(jwt, username, login, id);

                        Intent intent = new Intent(getBaseContext(), AccessPointsActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getBaseContext(), "An error has occurred! Please log in", Toast.LENGTH_SHORT).show();
                    loginBtn.setVisibility(View.VISIBLE);
                }
            });

            queue.add(loginRequest);
        }
    }
}