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
    // Views
    private PreviewView previewView;
    private OverlayView overlayView;
    private String username = "123";
    // Data Storage
    private AssessmentData currentAssessment = new AssessmentData(username);
    private Calendar baselineTestDate;
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