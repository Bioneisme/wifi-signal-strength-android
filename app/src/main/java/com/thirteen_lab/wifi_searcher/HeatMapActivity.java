package com.thirteen_lab.wifi_searcher;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thirteen_lab.wifi_searcher.utls.heat_map.GeographicalCalculator;
import com.thirteen_lab.wifi_searcher.utls.heat_map.GridView;
import com.thirteen_lab.wifi_searcher.utls.heat_map.MainData;
import com.thirteen_lab.wifi_searcher.utls.heat_map.WifiDetails;
import com.thirteen_lab.wifi_searcher.utls.heat_map.WifiNetwork;


import java.util.ArrayList;
import java.util.List;

public class HeatMapActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = HeatMapActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private boolean measurementStarted = false;

    private LocationRequest mLocationRequest;
    private WifiDetails wifiDetails;
    private WifiManager wifiManager;

    // UI elements
    private Spinner wifiNetworksSpinner;
    private Button measurementButton;
    private TextView lblLocation;
    private ViewGroup gridViewFrameLayout;

    private com.thirteen_lab.wifi_searcher.utls.heat_map.GridView gridView;

    private ArrayAdapter<WifiNetwork> wifiNetworksDataAdapter;

    // logic data:
    private MainData mainData = new MainData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);

        lblLocation = (TextView) findViewById(R.id.coordinates);
        wifiNetworksSpinner = (Spinner) findViewById(R.id.wifiNetworksSpinner);
        gridViewFrameLayout = (ViewGroup) findViewById(R.id.gridViewFrameLayout);

        wifiNetworksSpinner.setOnItemSelectedListener(this);
        wifiNetworksDataAdapter = new ArrayAdapter<WifiNetwork>(
                this, android.R.layout.simple_spinner_item,
                new ArrayList<WifiNetwork>());

        wifiNetworksDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wifiNetworksSpinner.setAdapter(wifiNetworksDataAdapter);

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiDetails = new ViewModelProvider(this).get(WifiDetails.class);
        wifiDetails.scanWifi(wifiManager);

        gridView = new GridView(this, mainData);
        gridViewFrameLayout.addView(gridView);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.heatMapWifi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.accessPoints:
                        startActivity(new Intent(getApplicationContext(), AccessPointsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.heatMapWifi:
                        return true;

                }
                return false;
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        WifiNetwork wifiNetwork = (WifiNetwork) parent.getItemAtPosition(position);
        gridView.update(wifiNetwork, null);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onStart() {
        super.onStart();

        mainData.startMeasurement(mLastLocation);
        if (mGoogleApiClient != null) {
            mRequestingLocationUpdates = true;
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void doMeasurement() {
        if  (!measurementStarted) {
            measurementStarted = true;
            mainData.startMeasurement(mLastLocation);
        }

        List<ScanResult> scanResults = this.wifiDetails.getScanResults();

        List<WifiNetwork> discoveredNetworks = new ArrayList<>();
        mainData.addMeasurement(mLastLocation, scanResults, discoveredNetworks);
        updateWifiNetworksSpinner(discoveredNetworks);

        gridView.update(null, null);
    }

    private void displayLocation() {
        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            String output = latitude + ", " + longitude;

            if (mainData.getGridInfo() != null) {
                Location centerLocation = mainData.getGridInfo().getCenterLocation();

                if (centerLocation != null) {
                    output += "\nCenter: " + centerLocation.getLatitude() + ", " + centerLocation.getLongitude();

                    output += "\nNorthwardsOffset (meters) = "
                            + GeographicalCalculator.InMeters.getNorthwardsDisplacement(centerLocation, mLastLocation);

                    output += "\nEastwardsOffset (meters) = "
                            + GeographicalCalculator.InMeters.getEastwardsDisplacement(centerLocation, mLastLocation);
                }
            }

            lblLocation.setText(output);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        // Location updates intervals in sec
        final int UPDATE_INTERVAL = 2000; // 2 sec
        final int FASTEST_INTERVAL = 1000; // 1 sec
        final int DISPLACEMENT = 1; // 1 meters

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                                "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        displayLocation();
        gridView.update(null, mLastLocation);
        doMeasurement();
    }

    private void updateWifiNetworksSpinner(List<WifiNetwork> discoveredNetworks) {
        for (WifiNetwork discoveredNetwork : discoveredNetworks) {
            wifiNetworksDataAdapter.add(discoveredNetwork);
        }

        wifiNetworksDataAdapter.notifyDataSetChanged();
    }
}