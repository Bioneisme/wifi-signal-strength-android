package com.thirteen_lab.wifi_searcher;

import static com.thirteen_lab.wifi_searcher.config.Constants.freqOfTone;
import static com.thirteen_lab.wifi_searcher.config.Constants.getFreqOfTone;
import static com.thirteen_lab.wifi_searcher.config.Constants.getRateUpdate;
import static com.thirteen_lab.wifi_searcher.config.Constants.setFreqOfTone;
import static com.thirteen_lab.wifi_searcher.config.Constants.setUpdateWifiRate;
import static com.thirteen_lab.wifi_searcher.config.Constants.updateWifiRate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.text.NumberFormat;

public class SettingsActivity extends AppCompatActivity {
    private Slider rateSlider;
    private Slider soundSlider;
    private ImageView sound_inc;
    private ImageView sound_dec;
    private ImageView rate_inc;
    private ImageView rate_dev;
    private TextView sound_result;
    private TextView rate_result;
    private Button logout;
    private Button save;

    private ImageView ruslangBtn;
    private ImageView englangBtn;


    private int soundValue;
    private int rateUpdate;

    private ImageView back;

    private SharedPreferences sharedPreferencesAuth;
    private SharedPreferences sharedPreferencesSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        soundSlider = findViewById(R.id.soundSlider);
        rateSlider = findViewById(R.id.rateSlider);
        sound_inc = findViewById(R.id.sound_add);
        sound_dec = findViewById(R.id.sound_remove);
        rate_inc = findViewById(R.id.rate_add);
        rate_dev = findViewById(R.id.rate_remove);
        sound_result = findViewById(R.id.sound_result);
        rate_result = findViewById(R.id.rate_result);
        back = findViewById(R.id.backBtn);
        logout = findViewById(R.id.logoutBtn);
        save = findViewById(R.id.saveStateBtn);

        ruslangBtn = findViewById(R.id.rus_lang_btn);
        englangBtn = findViewById(R.id.uk_lang_btn);

        sharedPreferencesSettings = getSharedPreferences("USER_SETTINGS", MODE_PRIVATE);
        sharedPreferencesAuth = getSharedPreferences("USER_AUTH", MODE_PRIVATE);

        soundValue = getFreqOfTone(sharedPreferencesSettings);
        rateUpdate = getRateUpdate(sharedPreferencesSettings);

        NumberFormat sliderFormat = NumberFormat.getInstance();
        sliderFormat.setMaximumFractionDigits(0);

        sound_result.setText(sliderFormat.format(soundValue));
        soundSlider.setValue(soundValue);
        soundSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                sound_result.setText(sliderFormat.format(value));
            }
        });

        sound_inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundValue = (int) soundSlider.getValue() + 1;
                if (soundValue > 2000) {
                    soundValue = 2000;
                }
                soundSlider.setValue(soundValue);
                sound_result.setText(sliderFormat.format(soundValue));
            }
        });

        sound_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundValue = (int) soundSlider.getValue() - 1;
                if (soundValue < 100) {
                    soundValue = 100;
                }
                soundSlider.setValue(soundValue);
                sound_result.setText(sliderFormat.format(soundValue));
            }
        });


        rate_result.setText(sliderFormat.format(rateUpdate));
        rateSlider.setValue(rateUpdate);
        rateSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                rate_result.setText(sliderFormat.format(value));
            }
        });

        rate_inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateUpdate = (int) rateSlider.getValue() + 1;
                if (rateUpdate > 3000) {
                    rateUpdate = 3000;
                }
                rateSlider.setValue(rateUpdate);
                rate_result.setText(sliderFormat.format(rateUpdate));
            }
        });

        rate_dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateUpdate = (int) rateSlider.getValue() - 1;
                if (rateUpdate < 600) {
                    rateUpdate = 600;
                }
                rateSlider.setValue(rateUpdate);
                rate_result.setText(sliderFormat.format(rateUpdate));
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("CommitPrefEdits")
                SharedPreferences.Editor editor = sharedPreferencesAuth.edit();
                editor.remove("jwt");
                editor.apply();

                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        ruslangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruslangBtn.setBackgroundColor(Color.GREEN);
                englangBtn.setBackgroundColor(Color.WHITE);
                LocalHelper.setLocale(SettingsActivity.this, "ru");
            }
        });

        englangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                englangBtn.setBackgroundColor(Color.GREEN);
                ruslangBtn.setBackgroundColor(Color.WHITE);
                LocalHelper.setLocale(SettingsActivity.this, "en");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFreqOfTone((int) soundSlider.getValue(), sharedPreferencesSettings);
                setUpdateWifiRate((int) rateSlider.getValue(), sharedPreferencesSettings);

                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}