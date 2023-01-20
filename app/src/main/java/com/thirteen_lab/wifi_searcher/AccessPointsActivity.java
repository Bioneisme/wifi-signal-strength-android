package com.thirteen_lab.wifi_searcher;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thirteen_lab.wifi_searcher.utls.access_points.Filter;
import com.thirteen_lab.wifi_searcher.utls.heat_map.WifiDetails;


import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class AccessPointsActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout apListWifi;
    private WifiDetails wifiDetails;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    private HashSet<Integer> filtersSet;
    private View filterView;
    private LayoutInflater inflater;
    final Handler handler = new Handler();
    private Runnable startDetect;


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            System.exit(-1);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_points);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Button filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFilterDialog();
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.accessPoints);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.accessPoints:
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.heatMapWifi:
                        startActivity(new Intent(getApplicationContext(), HeatMapActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        if (!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        this.inflater = (this).getLayoutInflater();
        this.filterView = inflater.inflate(R.layout.filter_detail, null);
        this.filtersSet = new HashSet<Integer>();
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        Button scanButton = findViewById(R.id.scanButton);
        this.apListWifi = findViewById(R.id.apListWifi);
        scanButton.setOnClickListener(this);
        this.wifiDetails = new ViewModelProvider(this).get(WifiDetails.class);
        this.updateAPList();
        this.updateLastScanTime();

        startDetect = new Runnable() {
            public void run() {
                if (wifiDetails.scanWifi(wifiManager)) {
                    updateLastScanTime();
                    updateAPList();
                }
                handler.postDelayed(this, 5000);
            }
        };

        handler.post(startDetect);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(startDetect);
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.removeCallbacks(startDetect);
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.round((Math.pow(10.0, exp) * 100.0)) / 100.0;
    }

    @SuppressLint({"StringFormatMatches", "WrongViewCast", "ClickableViewAccessibility", "ResourceType"})
    public void updateAPList() {
        List<ScanResult> scanResults = this.wifiDetails.getScanResults();

        Filter filterClass = new Filter(filtersSet, scanResults);
        filterClass.filterResults();
        scanResults = filterClass.getResults();

        LayoutInflater layoutInflater = getLayoutInflater();
        this.apListWifi.removeAllViews();
        if (scanResults == null) {
            layoutInflater.inflate(R.layout.no_ap_available, apListWifi, true);
        } else {
            scanResults.sort(Comparator.comparingInt(a -> a.level * (-1)));
            HorizontalScrollView cur_hsv;
            TextView cur_wifiName;
            TextView cur_distance = null;
            TextView cur_level = null;
            TextView cur_wifiAddress = null;
            TextView cur_wifiHZ = null;
            ImageView cur_wifi_image = new ImageView(AccessPointsActivity.this);

            for (ScanResult scanResult : scanResults) {
                if (!Objects.equals(scanResult.SSID, "")) {

                    cur_hsv = ((HorizontalScrollView) layoutInflater.inflate(R.layout.ap_detail, apListWifi, false));
                    cur_wifiName = (TextView) cur_hsv.findViewById(R.id.wifiName);
                    cur_wifiName.setText(getString(R.string.ap_name, scanResult.SSID + " (" + scanResult.capabilities + ")"));
                    cur_wifiHZ = (TextView) cur_hsv.findViewById(R.id.wifiFrequency);
                    cur_wifiHZ.setText(scanResult.frequency + "MHz");
                    cur_wifiAddress = (TextView) cur_hsv.findViewById(R.id.wifiAdress);
                    cur_wifiAddress.setText("(" + scanResult.BSSID + ")");
                    cur_distance = (TextView) cur_hsv.findViewById(R.id.distanceWifi);
                    cur_distance.setText(getString(R.string.ap_distance, calculateDistance(scanResult.level, scanResult.frequency)));
                    cur_level = (TextView) cur_hsv.findViewById(R.id.wifiLevel);
                    cur_level.setText(getString(R.string.ap_level, scanResult.level));
                    cur_wifi_image = (ImageView) cur_hsv.findViewById(R.id.wifiImage);

                    cur_hsv.setOnTouchListener(new WifiClickListener(scanResult.BSSID));

                    apListWifi.addView(cur_hsv);
                }
                if (!(scanResult.level == 0)) {
                    if (scanResult.level >= -40) {
                        cur_distance.setTextColor(Color.rgb(50, 205, 50));
                        cur_level.setTextColor(Color.rgb(50, 205, 50));
                        cur_wifi_image.setImageResource(R.drawable.ic_signal_wifi_4_bar);
                    }
                    if (scanResult.level <= -40 && scanResult.level >= -50) {
                        cur_distance.setTextColor(Color.rgb(76, 187, 23));
                        cur_level.setTextColor(Color.rgb(76, 187, 23));
                        cur_wifi_image.setImageResource(R.drawable.ic_signal_wifi_3_bar);
                    }
                    if (scanResult.level <= -50 && scanResult.level >= -80) {
                        cur_distance.setTextColor(Color.rgb(246, 190, 0));
                        cur_level.setTextColor(Color.rgb(246, 190, 0));
                        cur_wifi_image.setImageResource(R.drawable.ic_signal_wifi_2_bar);
                    }
                    if (scanResult.level <= -80 && scanResult.level >= -120) {
                        cur_distance.setTextColor(Color.rgb(196, 30, 58));
                        cur_level.setTextColor(Color.rgb(196, 30, 58));
                        cur_wifi_image.setImageResource(R.drawable.ic_signal_wifi_1_bar);
                    }
                    if (scanResult.level <= -120 && scanResult.level >= -200) {
                        cur_distance.setTextColor(Color.rgb(136, 8, 8));
                        cur_level.setTextColor(Color.rgb(136, 8, 8));
                        cur_wifi_image.setImageResource(R.drawable.ic_signal_wifi_0_bar);
                    }
                }
            }
        }
    }

    public void updateLastScanTime() {
        Date lastScanned = this.wifiDetails.getLastScanned();
        if (lastScanned != null) {
            ((TextView) findViewById(R.id.lastUpdatedTimeScan)).setText(
                    getString(
                            R.string.last_updated,
                            SimpleDateFormat.getTimeInstance().format(lastScanned)
                    )
            );
        }
    }

    public void onClickFilter(View view) {
        int id = view.getId();

        if (filtersSet.contains(id)) {
            filtersSet.remove(id);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.rgb(158,158,158));
            } else if (view instanceof  ImageView) {
                ((ImageView) view).setColorFilter(Color.rgb(158,158,158));
            }
        } else {
            filtersSet.add(view.getId());
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.rgb(144, 238, 144));
            } else if (view instanceof ImageView) {
                ((ImageView) view).setColorFilter(Color.rgb(144, 238, 144));
            }


        }
    }


    public void onClick(View view) {
        //Check if wifi is on!
        if (!this.wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), getString(R.string.turn_on_wifi), Toast.LENGTH_SHORT).show();
            return;
        }
        //Check if location is on!
        if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), getString(R.string.turn_on_location), Toast.LENGTH_SHORT).show();
            return;
        }
        //Start scanning
        if (wifiDetails.scanWifi(wifiManager)) {
            updateLastScanTime();
            updateAPList();
        }
    }


    private class WifiClickListener implements View.OnTouchListener {
        final String BSSID;

        public WifiClickListener(String BSSID) {
            this.BSSID = BSSID;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            long duration = motionEvent.getEventTime() - motionEvent.getDownTime();

            int CLICK_THRESHOLD = 100;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && duration < CLICK_THRESHOLD) {
                Intent intent = new Intent(AccessPointsActivity.this, DetectorActivity.class);
                intent.putExtra("bssid", BSSID);
                startActivity(intent);
            }
            return false;
        }
    }


    private void startFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter");
        builder.setIcon(R.drawable.ic_baseline_wifi_tethering_24);

        if (filterView.getParent() != null) {
            ((ViewGroup)filterView.getParent()).removeView(filterView);
        }
        builder.setView(filterView)
        .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        filterView = inflater.inflate(R.layout.filter_detail, null);
                        filtersSet.clear();
                        dialog.cancel();
                        if (wifiDetails.scanWifi(wifiManager)) {
                            updateLastScanTime();
                            updateAPList();
                        }
                    }
                })
        .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (wifiDetails.scanWifi(wifiManager)) {
                            updateLastScanTime();
                            updateAPList();
                        }
                    }
                });

        builder.setCancelable(true);
        builder.create();
        builder.show();
    }
}