package com.example.bioms1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bioms1.databinding.ActivityCameraBinding;

import java.net.URI;

public class camera extends AppCompatActivity {

    private static int CAMERA_PERMISSION_CODE = 100;
    private static int VIDEO_RECORD_CODE = 101;
    private Uri videoPath;

    private Button how, recordspeed, recordstiffness, recordBalanceSide, recordBalanceFront, recorddual, recordsensory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        initWidgets();

        how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(camera.this, demoSession.class);
                startActivity(intent);
            }
        });

        recordspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(camera.this, demoSession.class);
                startActivity(intent);
            }
        });

        if (isCameraPresentinPhone()) {
            Log.i("Video_record_tag", "Camera Detected");
            getCameraPermission();
        }
        else {
            Log.i("Video_record_tag", "No Camera Detected");
        }
    }

    private void initWidgets() {
        how = findViewById(R.id.how);
        recordspeed = findViewById(R.id.recordspeed);
        recordstiffness = findViewById(R.id.recordstiffness);
        recordBalanceSide = findViewById(R.id.recordBalanceSide);
        recordBalanceFront = findViewById(R.id.recordBalanceFront);
        recorddual = findViewById(R.id.recorddual);
        recordsensory = findViewById(R.id.recordsensory);

    }

    private boolean isCameraPresentinPhone() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }

        else {
            return false;
        }
    }

    public void  recordSensoryPressed(View view){
        recordVideo();
    }

    private void getCameraPermission (){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE );
        }
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_RECORD_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {

                videoPath = data.getData();
                Log.i("VIDEO_RECORD_TAG", "Video is recorded and available on path " + videoPath );
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.i("VIDEO_RECORD_TAG", "Video recording has cancelled");
        }
        else {
            Log.i("VIDEO_RECORD_TAG", "Video recording has run into an error");
        }
    }

}