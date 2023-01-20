package com.thirteen_lab.wifi_searcher.config;


import android.content.SharedPreferences;

public class Constants {
    public static final String Base_URL = "http://193.108.113.216:5000/api";
    public static final int freqOfTone = 540;
    public static final int updateWifiRate = 1000;


    public static int getFreqOfTone(SharedPreferences sharedPreferencesSettings) {
        int soundFreq = sharedPreferencesSettings.getInt("freqOfTone", 0);
        if (soundFreq == 0) {
            SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
            editor.putInt("freqOfTone", freqOfTone);
            editor.apply();
            soundFreq = freqOfTone;
        }

        return soundFreq;
    }

    public static int getRateUpdate(SharedPreferences sharedPreferencesSettings) {
        int rateUpdate = sharedPreferencesSettings.getInt("updateWifiRate", 0);
        if (rateUpdate == 0) {
            SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
            editor.putInt("updateWifiRate", updateWifiRate);
            editor.apply();
            rateUpdate = updateWifiRate;
        }

        return rateUpdate;
    }

    public static void setFreqOfTone(int freqOfTone, SharedPreferences sharedPreferencesSettings) {
        SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
        editor.putInt("freqOfTone", freqOfTone);
        editor.apply();
    }

    public static void setUpdateWifiRate(int updateWifiRate, SharedPreferences sharedPreferencesSettings) {
        SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
        editor.putInt("updateWifiRate", updateWifiRate);
        editor.apply();
    }
}
