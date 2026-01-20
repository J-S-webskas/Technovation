package com.example.myappication;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import java.util.List;
import com.google.mediapipe.framework.image.MPImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordFragment extends Fragment {
    // Permission Code
    private static final int PERMISSION_REQUEST_CAMERA = 101;

    // Views
    private PreviewView previewView;
    private OverlayView overlayView;
    private Button analyzeButton;

    // Camera & MediaPipe
    private ExecutorService cameraExecutor;
    private PoseLandmarker poseLandmarker;

    // Data Storage
    private AssessmentData currentAssessment;
    private String currentTestType = "none"; // "baseline", "dual", "sensory"
    private long testStartTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        view.setBackgroundColor(Color.RED);

        TextView testText = new TextView(getContext());
        testText.setText("TEST: Fragment Loaded!");
        testText.setTextColor(Color.WHITE);
        testText.setTextSize(30);
        testText.setGravity(Gravity.CENTER);

        ((ViewGroup) view).addView(testText);

        previewView = view.findViewById(R.id.camera_preview_view);
        overlayView = view.findViewById(R.id.pose_overlay_view);
        analyzeButton = view.findViewById(R.id.btn_analyze);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Request Camera Permissions
        if (hasCameraPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA
            );
        }

        // Initialize MediaPipe
        setupPoseDetector();

        // Initialize new assessment
        currentAssessment = new AssessmentData("patient_001");

        analyzeButton.setOnClickListener(v -> toggleAnalysis());
        return view;
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Log.e("RecordFragment", "Camera permission denied");
            // Inform user
        }
    }

    private void setupPoseDetector() {
        // Download pose_landmarker_lite.task from MediaPipe and place it in app/src/main/assets/
        String modelAssetPath = "pose_landmarker_lite.task";

        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelAssetPath)
                .build();

        // USE PoseLandmarker.PoseLandmarkerOptions (nested class)
        PoseLandmarker.PoseLandmarkerOptions options =
                PoseLandmarker.PoseLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setResultListener(this::processPoseResult)
                        .setErrorListener((error) -> Log.e("MediaPipe", "Error: " + error.getMessage()))
                        .build();

        try {
            poseLandmarker = PoseLandmarker.createFromOptions(requireContext(), options);
        } catch (Exception e) {
            Log.e("RecordFragment", "Failed to create PoseLandmarker", e);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Camera Selector (Back camera for better pose estimation)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Image Analysis for MediaPipe (Process each frame)
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(new Size(1280, 720))
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
                    if (poseLandmarker == null) {
                        imageProxy.close();
                        return;
                    }

                    // Convert ImageProxy to MediaPipe's MPImage
                    MPImage mediapipeImage = null;
                    try {
                        Bitmap bitmap = imageProxy.toBitmap();
                        BitmapImageBuilder imageBuilder = new BitmapImageBuilder(bitmap);
                        mediapipeImage = imageBuilder.build();
                        int rotation = imageProxy.getImageInfo().getRotationDegrees();

                        // RUN POSE DETECTION ON THIS FRAME
                        poseLandmarker.detectAsync(mediapipeImage, rotation);
                    } catch (Exception e) {
                        Log.e("RecordFragment", "Error processing frame", e);
                    } finally {
                        imageProxy.close(); // CRITICAL: Always close the ImageProxy
                    }
                });

                // Bind to lifecycle
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("RecordFragment", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processPoseResult(PoseLandmarkerResult result, MPImage image) {  // This runs on a background thread. Use landmarks for your MS analysis.
        if (result == null || result.landmarks().isEmpty()) return;

        List<NormalizedLandmark> landmarks = result.landmarks().get(0); // Get landmarks of the first (main) person

        // --- INSERT YOUR MS ANALYSIS CALCULATIONS HERE ---
        // 1. Calculate Center of Mass (CoM) - midpoint of hips (landmarks 23 & 24)
        NormalizedLandmark leftHip = landmarks.get(23);
        NormalizedLandmark rightHip = landmarks.get(24);
        PointF comPoint = new PointF(
                (leftHip.x() + rightHip.x()) / 2,
                (leftHip.y() + rightHip.y()) / 2
        );

        // 2. Calculate Base of Support (BoS) line - between heels (landmarks 29 & 30)
        NormalizedLandmark leftHeel = landmarks.get(29);
        NormalizedLandmark rightHeel = landmarks.get(30);
        PointF bosStart = new PointF(leftHeel.x(), leftHeel.y());
        PointF bosEnd = new PointF(rightHeel.x(), rightHeel.y());

        // 3. Calculate Joint Angles for Stiffness Analysis (Hip, Knee, Ankle)
        // Use = Python logic with landmarks 23, 25, 27 for left leg, etc.

        // --- UPDATE THE OVERLAY VIEW ON THE UI THREAD ---
        requireActivity().runOnUiThread(() -> {
            overlayView.updateLandmarksAndGraphics(landmarks, comPoint, bosStart, bosEnd);
            // Update TextViews with results (e.g., "Balance: STABLE", "Asymmetry: 5.2°")
        });
    }

    private void toggleAnalysis() {
        // Logic to start/stop recording and analysis sessions for your 6 tests
        Log.d("RecordFragment", "Analysis toggled - Connect to your test manager");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        cameraExecutor.shutdown();
    }
}