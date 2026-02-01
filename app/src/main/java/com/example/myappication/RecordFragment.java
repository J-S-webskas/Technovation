package com.example.myappication;

import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.mediapipe.framework.image.MPImage;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordFragment extends Fragment {
    private Handler autoStopHandler;
    private Runnable autoStopRunnable;
    private static final int BASELINE_DURATION_MS = 30000; // 30 seconds
    private static final int DUAL_TASK_DURATION_MS = 30000; // 30 seconds
    private static final int SENSORY_DURATION_MS = 30000; // 30 seconds
    // Permission Code
    private static final int PERMISSION_REQUEST_CAMERA = 101;

    // Views
    private PreviewView previewView;
    private OverlayView overlayView;
    private String username = "123";

    // Camera & MediaPipe
    private ExecutorService cameraExecutor;
    private PoseLandmarker poseLandmarker;

    // Data Storage
    private AssessmentData currentAssessment = new AssessmentData(username);
    private Calendar baselineTestDate;
    private Calendar dualTaskTestDate;
    private Calendar sensoryAtaxiaTestDate;
    private GaitMetrics currentGaitMetrics;
    private Map<String, ArrayDeque<Float>> angleHistories;
    private List<float[]> hipPositions;
    private float frameRate = 30f;

    // Test tracking
    private enum TestType { NONE, BASELINE, DUAL_TASK, SENSORY_ATAXIA }
    private TestType currentTestType = TestType.NONE; // NONE, BASELINE, DUAL_TASK, SENSORY_ATAXIA
    private DualTaskTest activeTest;
    private TestResult baselineResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        setupTestControls(view);

        previewView = view.findViewById(R.id.camera_preview_view);
        overlayView = view.findViewById(R.id.pose_overlay_view);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Request Camera Permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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

        // INITIALIZE GAIT ANALYSIS
        initializeGaitAnalysis();
        return view;
    }

    private void initializeGaitAnalysis() {
        angleHistories = new HashMap<>();
        angleHistories.put("left_hip", new ArrayDeque<>(100));
        angleHistories.put("right_hip", new ArrayDeque<>(100));
        angleHistories.put("left_knee", new ArrayDeque<>(100));
        angleHistories.put("right_knee", new ArrayDeque<>(100));
        angleHistories.put("left_ankle", new ArrayDeque<>(100));
        angleHistories.put("right_ankle", new ArrayDeque<>(100));

        hipPositions = new ArrayList<>();

        // Reset test state
        currentTestType = TestType.NONE;
        baselineResult = null;
        activeTest = null;
    }

    //// sets up the model and connects the mediapipe model for creating 3d pose skeleton
    private void setupPoseDetector() {
        // Download pose_landmarker_lite.task from MediaPipe and place it in app/src/main/assets/
        String modelAssetPath = "app/src/main/assets/pose_landmarker_lite.task";

        BaseOptions baseOptions = BaseOptions.builder()  // Handles fundamental model loading
                .setModelAssetPath(modelAssetPath)
                .build();

        // USE PoseLandmarker.PoseLandmarkerOptions (nested class)
        PoseLandmarker.PoseLandmarkerOptions options =
                PoseLandmarker.PoseLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.LIVE_STREAM)  // Real-time processing
                        .setResultListener(this::processPoseResult)  // Method that recieves pose detection results
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

    //// Calculates extracted landmarks from mediapipe, BoM, CoM, and angles from extractJointCoordinates. Passes these data to OverlayView to draw  3D skeletons
    private void processPoseResult(PoseLandmarkerResult result, MPImage image) {  // This runs on a background thread. Use landmarks for MS analysis.
        if (result == null || result.landmarks().isEmpty()) return; // If no person detected, skip processing.

        List<NormalizedLandmark> landmarks = result.landmarks().get(0); // Get landmarks of the first (main) person

        // Joint coordinate
        Map<String, float[]> joints = extractJointCoordinates(landmarks);

        // Only call if a test is actively happening
        if (currentTestType != TestType.NONE) {
            runGaitAnalysis(joints);
        }
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

//        requireActivity().runOnUiThread(() -> {
//            overlayView.updateLandmarksAndGraphics(landmarks, comPoint, bosStart, bosEnd);
//        });
    }

    //// Extracts angles only needed for gait into a map (removes facial expression, hands,)
    private Map<String, float[]> extractJointCoordinates(List<NormalizedLandmark> landmarks) {
        Map<String, float[]> joints = new HashMap<>();

        // Convert MediaPipe landmarks to [x, y] arrays
        // MediaPipe landmark indices: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker

        // Left leg
        joints.put("left_hip", new float[]{landmarks.get(23).x(), landmarks.get(23).y()});
        joints.put("left_knee", new float[]{landmarks.get(25).x(), landmarks.get(25).y()});
        joints.put("left_ankle", new float[]{landmarks.get(27).x(), landmarks.get(27).y()});
        joints.put("left_heel", new float[]{landmarks.get(29).x(), landmarks.get(29).y()});

        // Right leg
        joints.put("right_hip", new float[]{landmarks.get(24).x(), landmarks.get(24).y()});
        joints.put("right_knee", new float[]{landmarks.get(26).x(), landmarks.get(26).y()});
        joints.put("right_ankle", new float[]{landmarks.get(28).x(), landmarks.get(28).y()});
        joints.put("right_heel", new float[]{landmarks.get(30).x(), landmarks.get(30).y()});

        // Shoulders (for hip angle calculation)
        joints.put("left_shoulder", new float[]{landmarks.get(11).x(), landmarks.get(11).y()});
        joints.put("right_shoulder", new float[]{landmarks.get(12).x(), landmarks.get(12).y()});

        return joints;
    }

    //// Get the data calculated from GaitAnalyzer and stores it in GaitMetrics...
    private void runGaitAnalysis(Map<String, float[]> joints) {

        //// 1. Calculate metrics: angles
        float leftKneeAngle = GaitAnalyzer.calculateAngle(
                joints.get("left_hip"),
                joints.get("left_knee"),
                joints.get("left_ankle")
        );

        float rightKneeAngle = GaitAnalyzer.calculateAngle(
                joints.get("right_hip"),
                joints.get("right_knee"),
                joints.get("right_ankle")
        );

        // Hip angles (shoulder-hip-knee, like your Python code)
        float leftHipAngle = GaitAnalyzer.calculateAngle(
                joints.get("left_shoulder"),
                joints.get("left_hip"),
                joints.get("left_knee")
        );

        float rightHipAngle = GaitAnalyzer.calculateAngle(
                joints.get("right_shoulder"),
                joints.get("right_hip"),
                joints.get("right_knee")
        );

        // Ankle angles (simplified like Python)
        float leftAnkleAngle = 180 - leftKneeAngle;
        float rightAnkleAngle = 180 - rightKneeAngle;

        //// 2. Store in history
        angleHistories.get("left_knee").add(leftKneeAngle);
        angleHistories.get("right_knee").add(rightKneeAngle);
        angleHistories.get("left_hip").add(leftHipAngle);
        angleHistories.get("right_hip").add(rightHipAngle);
        angleHistories.get("left_ankle").add(leftAnkleAngle);
        angleHistories.get("right_ankle").add(rightAnkleAngle);

        //// 3. Store hip positions for speed calculation
        float[] hipPosition = {
                joints.get("left_hip")[0], joints.get("left_hip")[1],
                joints.get("right_hip")[0], joints.get("right_hip")[1]
        };
        hipPositions.add(hipPosition);

        //// 4. Calculate current gait metrics
        float currentSpeed = GaitAnalyzer.calculateWalkingSpeed(hipPositions, frameRate);

        Map<String, Float> stiffnessScores = new HashMap<>();
        for (String joint : Arrays.asList("left_knee", "right_knee", "left_hip", "right_hip")) {
            float stiffness = GaitAnalyzer.calculateMuscleStiffness(
                    angleHistories.get(joint), joint, frameRate
            );
            stiffnessScores.put(joint, stiffness);
        }

        Map<String, Float> asymmetryScores = new HashMap<>();

        // Knee asymmetry
        float kneeAsymmetry = GaitAnalyzer.calculateAsymmetry(
                angleHistories.get("left_knee"),
                angleHistories.get("right_knee"),
                "knee"
        );
        asymmetryScores.put("knee", kneeAsymmetry);

        // Hip asymmetry
        float hipAsymmetry = GaitAnalyzer.calculateAsymmetry(
                angleHistories.get("left_hip"),
                angleHistories.get("right_hip"),
                "hip"
        );
        asymmetryScores.put("hip", hipAsymmetry);

        //// 5. Update active test if running
        if (currentTestType != TestType.NONE && activeTest != null) {
            activeTest.addMeasurement(currentSpeed, stiffnessScores, asymmetryScores);
        }

        //// 6. Store metrics for UI display
        currentGaitMetrics = new GaitMetrics(
                currentSpeed,
                stiffnessScores.get("left_knee"),
                stiffnessScores.get("right_knee"),
                kneeAsymmetry,
                hipAsymmetry
        );
        currentGaitMetrics = new GaitMetrics(
                currentSpeed,
                stiffnessScores.get("left_knee"),
                stiffnessScores.get("right_knee"),
                kneeAsymmetry,
                hipAsymmetry
        );
    }

    // Simple data holder for UI
    private static class GaitMetrics {
        float speed;
        float leftKneeStiffness;
        float rightKneeStiffness;
        float kneeAsymmetry;
        float hipAsymmetry;

        GaitMetrics(float speed, float leftKnee, float rightKnee,
                    float kneeAsym, float hipAsym) {
            this.speed = speed;
            this.leftKneeStiffness = leftKnee;
            this.rightKneeStiffness = rightKnee;
            this.kneeAsymmetry = kneeAsym;
            this.hipAsymmetry = hipAsym;
        }
    }

    // In your onCreateView, add test control buttons
    private void setupTestControls(View view) {
        Button btnBaseline = view.findViewById(R.id.btn_baseline);
        Button btnDualTask = view.findViewById(R.id.btn_dual_task);
        Button btnSensory = view.findViewById(R.id.btn_sensory);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnBaseline.setOnClickListener(v -> startBaselineTest());
        btnDualTask.setOnClickListener(v -> startDualTaskTest());
        btnSensory.setOnClickListener(v -> startSensoryAtaxiaTest());
        btnCancel.setOnClickListener(v -> stopCurrentTestAndDiscardData());
    }

    private void stopCurrentTestAndDiscardData() {
        if (currentTestType == TestType.NONE) {
            Toast.makeText(requireContext(), "No test running", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancel the auto-stop timer
        if (autoStopHandler != null && autoStopRunnable != null) {
            autoStopHandler.removeCallbacks(autoStopRunnable);
            autoStopHandler = null;
            autoStopRunnable = null;
        }

        // Clear test state
        TestType cancelledTest = currentTestType;
        currentTestType = TestType.NONE;
        activeTest = null;

        // Clear measurement data
        initializeGaitAnalysis();

        String testName = getTestName(cancelledTest);
        Toast.makeText(requireContext(), testName + " test cancelled - results not saved", Toast.LENGTH_SHORT).show();
    }

    // Helper method
    private String getTestName(TestType testType) {
        switch (testType) {
            case BASELINE:
                return "Baseline";
            case DUAL_TASK:
                return "Dual task";
            case SENSORY_ATAXIA:
                return "Sensory ataxia";
            default:
                return "Test";
        }
    }

    private void startBaselineTest() {
        if (currentTestType != TestType.NONE) { // If the user clicks another test while this test is running, discard this test data
            stopCurrentTestAndDiscardData();
            return;
        }
        if (!currentAssessment.canTakeBaselineTest()) {
            Toast.makeText(requireContext(), "Baseline test was already completed for today. Record again tomorrow!", Toast.LENGTH_SHORT).show();
        }
        // Clear old data
        initializeGaitAnalysis();

        // Start baseline recording
        currentTestType = TestType.BASELINE;
        baselineTestDate = Calendar.getInstance();

        Toast.makeText(requireContext(), "Baseline test started - Walk normally for 30 seconds", Toast.LENGTH_SHORT).show();

        var auto_test_handler = new Handler(Looper.getMainLooper());
        var auto_test_runner = new Runnable() {
            @Override
            public void run() {
                if (currentTestType == TestType.BASELINE) {
                    Toast.makeText(requireContext(), "Baseline test completed!",
                            Toast.LENGTH_SHORT).show();
                    saveBaselineResults();
                    currentTestType = TestType.NONE;
                }
            }
        };
        auto_test_handler.postDelayed(auto_test_runner, BASELINE_DURATION_MS ); // 30 seconds = 30000ms
    }

    private void startDualTaskTest() {
        if (currentTestType != TestType.NONE) {
            stopCurrentTestAndDiscardData();
            return;
        }

        if (baselineResult == null) {
            Toast.makeText(requireContext(), "Record baseline first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentAssessment.canTakeDualTaskTest()) {
            Toast.makeText(requireContext(), "Dual task test was already completed for today. Record again tomorrow!", Toast.LENGTH_SHORT).show();
        }

        // Create dual task test
        activeTest = new DualTaskTest(baselineResult);
        activeTest.startTest();
        currentTestType = TestType.DUAL_TASK;

        Toast.makeText(requireContext(), "Dual task test started - Count backwards from 30", Toast.LENGTH_SHORT).show();

        autoStopHandler = new Handler(Looper.getMainLooper());
        autoStopRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentTestType == TestType.DUAL_TASK) {
                    Toast.makeText(requireContext(), "Dual task test completed!",
                            Toast.LENGTH_SHORT).show();
                    saveOtherTestResults(currentTestType);
                    currentTestType = TestType.NONE;
                }
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, DUAL_TASK_DURATION_MS ); // 30 seconds = 30000ms
    }

    private void startSensoryAtaxiaTest() {
        if (currentTestType != TestType.NONE) {
            stopCurrentTestAndDiscardData();
            return;
        }

        if (baselineResult == null) {
            Toast.makeText(requireContext(), "Record baseline test first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentAssessment.canTakeSensoryAtaxiaTest()) {
            Toast.makeText(requireContext(), "Sensory ataxia test was already completed for today. Record again tomorrow!", Toast.LENGTH_SHORT).show();
        }

        // Create sensory ataxia test
        activeTest = new DualTaskTest(baselineResult);
        activeTest.startTest();
        currentTestType = TestType.SENSORY_ATAXIA;

        Toast.makeText(requireContext(), "Sensory ataxia test started - Close your eyes and walk", Toast.LENGTH_SHORT).show();

        autoStopHandler = new Handler(Looper.getMainLooper());
        autoStopRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentTestType == TestType.SENSORY_ATAXIA) {
                    Toast.makeText(requireContext(), "Sensory ataxia test completed!",
                            Toast.LENGTH_SHORT).show();
                    saveOtherTestResults(currentTestType);
                    currentTestType = TestType.NONE;
                }
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, SENSORY_DURATION_MS ); // 30 seconds = 30000ms
        currentAssessment.setSensoryAtaxiaTestDate(new Date());
    }

    private void saveOtherTestResults(TestType currentTestType) {
        // Calculate metrics (same as baseline
        // TODO: DECIDE IF WE KEEP MY IMPLEMENTATION OR MANREEN'S
        float currentSpeed = GaitAnalyzer.calculateWalkingSpeed(hipPositions, frameRate);
        // Calculate walking speed comparison with baseline
        TestComparisons comparisons = currentAssessment.getComparisons();
        if (currentAssessment.getBaseline().getWalkingSpeed() > 0) {
            float baselineSpeed = currentAssessment.getBaseline().getWalkingSpeed();
            float speedChange = ((currentSpeed - baselineSpeed) / baselineSpeed) * 100;
            if (currentTestType == TestType.SENSORY_ATAXIA) {
                comparisons.setSensoryAtaxiaSpeedChange(speedChange);
            } else {
                comparisons.setDualTaskSpeedChange(speedChange);
            }
        }
        // TODO: NEED BALANCE
//        float currentBalance = GaitAnalyzer.calculateBalance(, );
        //TODO: IF HAVE TIME, ADD HIP
        Map<String, Float> stiffnessScores = new HashMap<>();
        for (String joint : Arrays.asList("left_knee", "right_knee")) {
            float stiffness = GaitAnalyzer.calculateMuscleStiffness(
                    angleHistories.get(joint), joint, frameRate
            );
            stiffnessScores.put(joint, stiffness);
        }
        if (!currentAssessment.getBaseline().getStiffness().isEmpty()) {
            float baselineStiffnessForKnee = currentAssessment.getBaseline().getStiffness().get("knee");
            float stiffnessChangeForKnee = (stiffnessScores.get("knee") - baselineStiffnessForKnee) / baselineStiffnessForKnee * 100;
            if (currentTestType == TestType.SENSORY_ATAXIA) {
                comparisons.setSensoryAtaxiaStiffnessChange(stiffnessChangeForKnee);
            } else {
                comparisons.setDualTaskStiffnessChange(stiffnessChangeForKnee);
            }
        }
        // Calculates asymmetry for knee
        //TODO: IF HAVE TIME, ADD HIP
        Map<String, Float> asymmetryScores = new HashMap<>();
        float kneeAsymmetry = GaitAnalyzer.calculateAsymmetry(
                angleHistories.get("left_knee"),
                angleHistories.get("right_knee"),
                "knee"
        );
//        if (!currentAssessment.getBaseline().getAsymmetry().isEmpty()) {
//            float baselineStiffnessForKnee = currentAssessment.getBaseline().getStiffness().get("knee");
//            float stiffnessChangeForKnee = (stiffnessScores.get("knee") - baselineStiffnessForKnee) / baselineStiffnessForKnee * 100;
//            if (currentTestType == TestType.SENSORY_ATAXIA) {
//                comparisons.setSensoryAtaxiaStiffnessChange(stiffnessChangeForKnee);
//            } else {
//                comparisons.setDualTaskStiffnessChange(stiffnessChangeForKnee);
//            }
//        }

        // Save to currentAssessment's SENSORY_ATAXIA & DUAL_TASK section. IS THIS NECESSARY?
//        if (currentTestType == TestType.SENSORY_ATAXIA) {
//            currentAssessment.getSensoryAtaxia().setWalkingSpeed(currentSpeed);
//            currentAssessment.getSensoryAtaxia().setStiffness(stiffnessScores);
//            currentAssessment.getSensoryAtaxia().setAsymmetry(asymmetryScores);
//        } else {
//            currentAssessment.getDualTask().setWalkingSpeed(currentSpeed);
//            currentAssessment.getDualTask().setStiffness(stiffnessScores);
//            currentAssessment.getDualTask().setAsymmetry(asymmetryScores);
//        }

        // TODO: CALCULATE BALANCE
//        if (currentAssessment.getBaseline().getBalanceScore() > 0) {
//            float baselineBalance = currentAssessment.getBaseline().getBalanceScore();
//            float balanceChange = ((currentBalance - baselineBalance) / baselineBalance) * 100;
//            comparisons.setSensoryAtaxiaBalanceChange(balanceChange);
//        }
        // TODO: Calculate endurance changes for sensory ataxia
//        if (currentAssessment.getBaseline().getEnduranceScore() > 0) {
//            float baselineEndurance = currentAssessment.getBaseline().getEnduranceScore();
//            float EnduranceChange = ((currentEndurance - baselineEndurance) / baselineEndurance) * 100;
//            comparisons.setSensoryAtaxiaEnduranceChange(enduranceChange);
//        }
    }

    // TODO: baseline currently only saves muscle stiffness and asymmetry. It does not save speed, balance, or endurance (code needed from Manreen)
    private void saveBaselineResults() {
        float currentSpeed = GaitAnalyzer.calculateWalkingSpeed(hipPositions, frameRate);
        //TODO: IF HAVE TIME, ADD HIP
        Map<String, Float> stiffnessScores = new HashMap<>();
        for (String joint : Arrays.asList("left_knee", "right_knee")) {
            float stiffness = GaitAnalyzer.calculateMuscleStiffness(
                    angleHistories.get(joint), joint, frameRate
            );
            stiffnessScores.put(joint, stiffness);
        }
        //TODO: IF HAVE TIME, ADD HIP
        Map<String, Float> asymmetryScores = new HashMap<>();
        float kneeAsymmetry = GaitAnalyzer.calculateAsymmetry(
                angleHistories.get("left_knee"),
                angleHistories.get("right_knee"),
                "knee"
        );
        asymmetryScores.put("knee", kneeAsymmetry);

        Toast.makeText(requireContext(), "Baseline recorded!", Toast.LENGTH_SHORT).show();

        // Save to database
        currentAssessment.getBaseline().setWalkingSpeed(currentSpeed);
        currentAssessment.getBaseline().setStiffness(stiffnessScores);
        currentAssessment.getBaseline().setAsymmetry(asymmetryScores);
        currentAssessment.setStatus(AssessmentData.AssessmentStatus.BASELINE_COMPLETED);
        currentAssessment.setBaselineTestDate(new Date());

        // SAVE USING DataStorageManager
        DataStorageManager.saveAssessment(requireContext(), currentAssessment);

        Toast.makeText(requireContext(), "Baseline test saved. Next test available tomorrow.", Toast.LENGTH_LONG).show();
    }

}