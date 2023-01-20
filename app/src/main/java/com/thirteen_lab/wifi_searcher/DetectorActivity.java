package com.thirteen_lab.wifi_searcher;

import static com.thirteen_lab.wifi_searcher.config.Constants.getFreqOfTone;
import static com.thirteen_lab.wifi_searcher.config.Constants.getRateUpdate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.thirteen_lab.wifi_searcher.config.Constants;
import com.thirteen_lab.wifi_searcher.utls.detector.KeyValue;
import com.thirteen_lab.wifi_searcher.utls.heat_map.WifiDetails;

import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DetectorActivity extends AppCompatActivity implements SensorEventListener {
    private WifiDetails wifiDetails;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private LocationManager locationManager;
    private String BSSID;
    public boolean stopDetect;
    private double savedDistance;
    private int savedDistanceCount;
    private ProgressBar progressBar;
    private ObjectAnimator progressAnimator;
    private TextView wifiDB;
    private TextView wifiState;
    private TextView wifiName;
    private TextView wifiData;
    private double wifiDistance;
    private int wifiFrequency;
    private int wifiLevel;
    private String wifiSecurity;
    private int freqOfTone;
    private int updateWifiRate;
    private SensorManager sensorManager;
    private ImageView wifiCompass;
    private float DegreeStart = 0f;
    private TextView wifiHorizon;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private KeyValue<Integer, Integer> degreesPair;
    private ToneGenerator toneGenerator;

    private float[] gravityData = new float[3];
    private float[] geomagneticData = new float[3];
    private boolean hasGravityData = false;
    private boolean hasGeomagneticData = false;
    private double rotationInDegrees;


    final Handler handler = new Handler();

    Runnable startDetect = new Runnable() {
        public void run() {
            if (savedDistanceCount >= 500) {
                Toast.makeText(getApplicationContext(), getString(R.string.wifi_not_found),
                        Toast.LENGTH_SHORT).show();
                stopDetect = true;
                Intent intent = new Intent(DetectorActivity.this, AccessPointsActivity.class);
                startActivity(intent);
                return;
            }

            if (!stopDetect) {
                if (wifiDetails.scanWifi(wifiManager)) {
                    detectWifi();
                }
                handler.postDelayed(this, updateWifiRate);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        this.wifiDetails = new ViewModelProvider(this).get(WifiDetails.class);
        this.wifiInfo = wifiManager.getConnectionInfo();
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        SharedPreferences sharedPreferencesSettings = getSharedPreferences("USER_SETTINGS", MODE_PRIVATE);

        Intent intent = getIntent();
        this.BSSID = intent.getStringExtra("bssid");

        wifiData = findViewById(R.id.wifiInfo);
        wifiDB = findViewById(R.id.wifiDB);
        wifiName = findViewById(R.id.wifiName);
        wifiState = findViewById(R.id.wifiState);
        wifiHorizon = findViewById(R.id.wifiHorizon);
        wifiCompass = findViewById(R.id.wifiCompass);

        wifiData.setText(BSSID);
        freqOfTone = getFreqOfTone(sharedPreferencesSettings);
        updateWifiRate = getRateUpdate(sharedPreferencesSettings);

        Button stopButton = findViewById(R.id.stopDetectButton);
        stopButton.setOnClickListener(new DetectButtonClickListener());
        handler.post(startDetect);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        savedDistance = 100;
        savedDistanceCount = 0;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        degreesPair = new KeyValue<>(0, -100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, gravityData, 0, 3);
                hasGravityData = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, geomagneticData, 0, 3);
                hasGeomagneticData = true;
                break;
            default:
                return;
        }

        if (hasGravityData && hasGeomagneticData) {
            float identityMatrix[] = new float[9];
            float rotationMatrix[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, identityMatrix,
                    gravityData, geomagneticData);

            if (success) {
                float orientationMatrix[] = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationMatrix);
                float rotationInRadians = orientationMatrix[0];
                rotationInDegrees = Math.toDegrees(rotationInRadians);

                int intDegree = (int) rotationInDegrees;
                if (degreesPair.getValue() < wifiLevel) {
                    degreesPair.setKey(intDegree);
                    degreesPair.setValue(wifiLevel);
                    wifiHorizon.setText(wifiLevel + "dB - " + intDegree + "°");
                }

                if (Math.abs(DegreeStart - -intDegree) > 5) {
                    RotateAnimation ra = new RotateAnimation(
                            DegreeStart,
                            -degreesPair.getKey(),
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setFillAfter(true);
                    ra.setDuration(210);
                    wifiCompass.startAnimation(ra);
                    DegreeStart = -intDegree;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.round((Math.pow(10.0, exp) * 100.0)) / 100.0;
    }

    public void detectWifi() {
        List<ScanResult> scanResults = this.wifiDetails.getScanResults();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (scanResults == null) {
            return;
        }

        for (ScanResult scanResult : scanResults) {
            double signalDistance = calculateDistance(scanResult.level, scanResult.frequency);
            if (Objects.equals(scanResult.BSSID, this.BSSID)) {
                savedDistanceCount = 0;

                wifiDB.setText(getString(R.string.ap_level, scanResult.level));
                wifiName.setText(getString(R.string.ap_name, scanResult.SSID));
                wifiSecurity = scanResult.capabilities;
                wifiLevel = scanResult.level;
                wifiFrequency = scanResult.frequency;
                savedDistance = signalDistance;
                playSound(signalDistance);
            } else {
                savedDistanceCount += 1;
                playSound(savedDistance);
            }
        }
    }

    public void startTone(int numTones, int sound, int val) {
        try {
            new CountDownTimer(updateWifiRate, (updateWifiRate / numTones)) {
                @Override
                public void onTick(long l) {
                    if (toneGenerator == null) {
                        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    }
                    toneGenerator.startTone(sound, val);
                }

                @Override
                public void onFinish() {
                    if (toneGenerator != null) {
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }
            }.start();
        } catch (Exception e) {
            System.out.println("startTone Error: " + e);
        }
    }

    @SuppressLint("SetTextI18n")
    public void playSound(double signalDistance) {
        wifiDistance = signalDistance;
        if (signalDistance >= 100) {
            startTone(1, ToneGenerator.TONE_PROP_PROMPT, updateWifiRate / 2);
            wifiState.setText("Very-very far");
            progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0);
            progressAnimator.setDuration(200);
            progressAnimator.start();
        } else if (signalDistance <= 1) {
            startTone(4, ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT, updateWifiRate / 4);
            wifiState.setText("Very-very near");
            progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 100);
            progressAnimator.setDuration(200);
            progressAnimator.start();
        } else {
            startTone(2, ToneGenerator.TONE_PROP_PROMPT, (int) ((updateWifiRate / 2) + signalDistance));
            progressAnimator = ObjectAnimator.ofInt(progressBar, "progress",
                    (int) (100 - signalDistance));
            progressAnimator.setDuration(200);
            progressAnimator.start();

            if (signalDistance > 50) {
                wifiState.setText("Very far");
            } else if (signalDistance > 30) {
                wifiState.setText("Far");
            } else if (signalDistance > 10) {
                wifiState.setText("Near");
            } else {
                wifiState.setText("Very near");
            }
        }
    }


    private class DetectButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            stopDetect = true;
            Intent intent = new Intent(DetectorActivity.this, AccessPointsActivity.class);
            startActivity(intent);
            double lat = 0, lng = 0;
            float acc = 0;
            if (ActivityCompat.checkSelfPermission(DetectorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetectorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.turn_on_location), Toast.LENGTH_SHORT).show();
            } else {
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null && loc.hasAltitude()) {
                    lat = loc.getLatitude();
                    lng = loc.getLongitude();
                    acc = loc.getAccuracy();
                }
            }
            RequestQueue queue = Volley.newRequestQueue(DetectorActivity.this);
            SharedPreferences sharedPreferences = getSharedPreferences("USER_AUTH", MODE_PRIVATE);

            Map<String, String> paramsDetect = new HashMap<>();
            paramsDetect.put("name", (String) wifiName.getText());
            paramsDetect.put("bssid", BSSID);
            paramsDetect.put("distance", String.valueOf(wifiDistance));
            paramsDetect.put("level", String.valueOf(wifiLevel));
            paramsDetect.put("security", wifiSecurity);
            paramsDetect.put("frequency", String.valueOf(wifiFrequency));
            paramsDetect.put("id", String.valueOf(sharedPreferences.getInt("id", 0)));
            paramsDetect.put("lat", String.valueOf(lat));
            paramsDetect.put("lng", String.valueOf(lng));
            paramsDetect.put("acc", String.valueOf(acc));

            JSONObject objectDetect = new JSONObject(paramsDetect);
            JsonObjectRequest detectRequest = new JsonObjectRequest(Request.Method.POST,
                    Constants.Base_URL + "/wifi/postWifi",
                    objectDetect, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getBaseContext(), "The WiFi data was recorded", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getBaseContext(), "An error occurred while recording the WiFi data",
                            Toast.LENGTH_LONG).show();
                }
            });

            queue.add(detectRequest);
        }
    }
}