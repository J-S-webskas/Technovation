package com.example.myappication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Speed extends AppCompatActivity implements LocationListener {

    public static float currentSpeed = 0.0f;
    public static float totalDistance = 0.0f;
    public static float finalDistance = 0.0f;

    private TextView localityTV, speedTV, averageSpeedTV, maxSpeedTV, distanceTV;
    private LocationManager locationManager;
    private Location previousLocation;

    private float maxSpeed = 0.0f;
    private final ArrayList<Float> speedList = new ArrayList<>();

    private Handler startHandler = new Handler();
    private Runnable startRunnable;

    // Permission launcher
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startLocationUpdates();
                        } else {
                            Toast.makeText(this,
                                    "Location permission is required for speed test",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        localityTV = findViewById(R.id.localityTV);
        speedTV = findViewById(R.id.speedTV);
        averageSpeedTV = findViewById(R.id.avgSpeedTV);
        maxSpeedTV = findViewById(R.id.maxSpeedTV);
        distanceTV = findViewById(R.id.distanceTV);

        Button cancelBtn = findViewById(R.id.cancelBtn);
        Button restartBtn = findViewById(R.id.restartBtn);
        Button saveBtn = findViewById(R.id.saveBtn);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Toast.makeText(this,
                "Speed test will start in 10 seconds...",
                Toast.LENGTH_SHORT).show();

        startRunnable = this::checkPermissionAndStart;
        startHandler.postDelayed(startRunnable, 10000);

        cancelBtn.setOnClickListener(v -> cancelTest());
        restartBtn.setOnClickListener(v -> restartTest());
        saveBtn.setOnClickListener(v -> saveAndExit());
    }

    private void checkPermissionAndStart() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            startLocationUpdates();

        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        Toast.makeText(this,
                "Speed test started!",
                Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    1,
                    this
            );
        } catch (SecurityException e) {
            Toast.makeText(this,
                    "Location permission missing",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private float calculateAverageSpeed() {
        if (speedList.isEmpty()) return 0.0f;

        float sum = 0.0f;
        for (float s : speedList) {
            sum += s;
        }
        return sum / speedList.size();
    }

    private String getLocalityFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1);

            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Unknown Location";
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        float speed = location.getSpeed() * 3.6f;
        speedList.add(speed);

        if (speed > maxSpeed) {
            maxSpeed = speed;
        }

        currentSpeed = calculateAverageSpeed();

        if (previousLocation != null) {
            float distance = previousLocation.distanceTo(location);
            totalDistance += distance / 1000.0f;
        }

        previousLocation = location;

        speedTV.setText(String.format(Locale.getDefault(),
                "Speed: %.2f km/h", speed));

        averageSpeedTV.setText(String.format(Locale.getDefault(),
                "%.2f", currentSpeed));

        maxSpeedTV.setText(String.format(Locale.getDefault(),
                "%.2f", maxSpeed));

        distanceTV.setText(String.format(Locale.getDefault(),
                "%.2f km", totalDistance));

        localityTV.setText(getLocalityFromLocation(location));

        finalDistance = totalDistance;
    }

    /* ================================
       CLEAR EVERYTHING COMPLETELY
       ================================ */
    private void clearAllStoredData() {

        // Stop GPS updates
        locationManager.removeUpdates(this);
        startHandler.removeCallbacks(startRunnable);

        // Reset static variables
        currentSpeed = 0.0f;
        totalDistance = 0.0f;
        finalDistance = 0.0f;

        // Reset instance variables
        maxSpeed = 0.0f;
        speedList.clear();
        previousLocation = null;

        // Reset UI
        speedTV.setText("Speed: 0.0 km/h");
        averageSpeedTV.setText("0");
        maxSpeedTV.setText("0");
        distanceTV.setText("0");
        localityTV.setText("Location");
    }

    private void cancelTest() {

        clearAllStoredData();

        Toast.makeText(this,
                "Speed test canceled",
                Toast.LENGTH_SHORT).show();

        finish(); // Navigate back to RecordFragment
    }

    private void restartTest() {

        clearAllStoredData();

        Toast.makeText(this,
                "Restarting in 10 seconds...",
                Toast.LENGTH_SHORT).show();

        startRunnable = this::checkPermissionAndStart;
        startHandler.postDelayed(startRunnable, 10000);
    }

    private void saveAndExit() {

            // Stop location updates but keep clear stored values
            locationManager.removeUpdates(this);
            startHandler.removeCallbacks(startRunnable);

            Toast.makeText(this,
                    "Speed data saved",
                    Toast.LENGTH_SHORT).show();

            // Go back to RecordFragment
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        startHandler.removeCallbacks(startRunnable);
    }
}