package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WifiDetails extends ViewModel {
    private List<ScanResult> scanResults = null;
    private Date lastScanned = null;

    public boolean scanWifi(WifiManager wifiManager) {
        boolean scanned = wifiManager.startScan();
        this.scanResults = wifiManager.getScanResults();
        this.lastScanned = Calendar.getInstance().getTime();
        return scanned;
    }

    public Date getLastScanned() {
        return this.lastScanned;
    }

    public List<ScanResult> getScanResults() {
        return this.scanResults;
    }
}