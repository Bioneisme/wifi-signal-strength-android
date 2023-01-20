package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;
import android.net.wifi.ScanResult;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainData {
    private GridInfo gridInfo;
    private Map<WifiNetwork, SignalGrid> signalGrids = new HashMap<>();

    public MainData() {

    }

    public void startMeasurement(Location location) {
        gridInfo =new GridInfo(location);
    }

    // the only data-updating method:
    public boolean addMeasurement(Location location,
                                  List<ScanResult> scanResults,
                                  List<WifiNetwork> discoveredNetworks) {
        if (gridInfo == null)
            return false;

        for (ScanResult scanResult : scanResults) {
            WifiNetwork wifiNetwork = new WifiNetwork(scanResult.BSSID, scanResult.SSID);

            if (!signalGrids.containsKey(wifiNetwork)) {
                signalGrids.put(wifiNetwork, new SignalGrid(gridInfo));
                discoveredNetworks.add(wifiNetwork);
            }

            SignalGrid signalGrid = signalGrids.get(wifiNetwork);
            signalGrid.addMeasurement(location, scanResult);
        }

        return true;
    }

    public GridInfo getGridInfo() {
        return gridInfo;
    }

    public Map<WifiNetwork, SignalGrid> getSignalGrids() {
        return signalGrids;
    }
}
