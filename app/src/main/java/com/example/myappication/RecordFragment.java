package com.example.myappication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

/*
Saving to AssessmentData
Calling DataStorageManager.saveAssessment()

TODO: WHAT TO DO WITH AUTOSTOPHANDLER?
 */
public class RecordFragment extends Fragment {
    private Handler autoStopHandler;
    private Runnable autoStopRunnable;
    private static final int BASELINE_DURATION_MS = 15000; // 15 seconds
    private static final int DUAL_TASK_DURATION_MS = 15000; // 15 seconds
    private static final int SENSORY_DURATION_MS = 15000; // 15 seconds
    // Permission Code
    // Views
   private String username = "123";
   private static float frameRate = 0;
    // Data Storage
    private AssessmentData currentAssessment = new AssessmentData(username);
    private Calendar baselineTestDate;

    // TODO: (MANREEN) GUIDELINE: CREATE OWN INSTANCE VARIABLE TO STORE THE ANGLE FOR BALANCE ETC
    private static Map<String, ArrayDeque<Float>> angleHistories = new HashMap<>(); // stores knee angles for one recording session at a time.

    // Test tracking
    private enum TestType { NONE, BASELINE, DUAL_TASK, SENSORY_ATAXIA }  // Manage three test types. Ensure they are performed in order (baseline first, then others).
    private TestType currentTestType = TestType.NONE; // NONE, BASELINE, DUAL_TASK, SENSORY_ATAXIA
    private DualTaskTest activeTest;
    private TestResult baselineResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        setupTestControls(view);
        return view;
    }

    private void setupTestControls(View view) {
        CardView btnBaseline = view.findViewById(R.id.btn_baseline);
        CardView btnDualTask = view.findViewById(R.id.btn_dual_task);
        CardView btnSensory = view.findViewById(R.id.btn_sensory);
        CardView btnSpeed = view.findViewById(R.id.btn_walking_speed_and_endurance);

        btnBaseline.setOnClickListener(v -> startBaselineTest());
        btnDualTask.setOnClickListener(v -> startDualTaskTest());
        btnSensory.setOnClickListener(v -> startSensoryAtaxiaTest());
        // TODO: SPEED ON CLICK LISTENER
    }

    // TODO: (MANREEN) GUIDELINE: CREATE OWN METHOD FOR ADDING THESE ANGLES FROM POSELANDMARKERHELPER TO INSTANCE VARIABLE

    // Add angle per frame from PoseLandmarkerHelper to Map<String, ArrayDeque<Float>> angleHistories. Called from PoseLandmarkerHlper
    public static void addAnglePerFrame(String name, float angle) {
        // Gets reference to the ArrayDeque inside of angleHistories
        ArrayDeque<Float> angles = angleHistories.get(name);
        if (angles == null) {
            // The angleHistories's arrayDeque is created if there isn't anything
            angles = new ArrayDeque<>();
            // Store the reference IN the map (key = name, value = deque)
            angleHistories.put(name, angles);
        }
        angles.add(angle);  // updates the deque that angleHistory.get(name) references
    }
    public void stopCurrentTestAndDiscardData() {
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
        CameraFragment cameraFragment = (CameraFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        cameraFragment.stopCamera();
        // Clear test state
        TestType cancelledTest = currentTestType;
        currentTestType = TestType.NONE;
        activeTest = null;

        if (angleHistories != null) {
            angleHistories.clear();
        }

        String testName = getTestName(cancelledTest);
        requireActivity().getSupportFragmentManager().popBackStack();
        Toast.makeText(requireContext(), testName + " test cancelled - results not saved", Toast.LENGTH_SHORT).show();
    }

    // Helper method
    private static String getTestName(TestType testType) {
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

        // TODO: CALL CAMERA
        // Start baseline recording
        currentTestType = TestType.BASELINE;
        baselineTestDate = Calendar.getInstance();

        Toast.makeText(requireContext(), "Baseline test started - Walk normally for 30 seconds", Toast.LENGTH_SHORT).show();

        var auto_test_handler = new Handler(Looper.getMainLooper());
        var auto_test_runner = new Runnable() {
            @Override
            public void run() {
                if (currentTestType == TestType.BASELINE) {
                    try {
                        saveBaselineResults();

                        CameraFragment cameraFragment = (CameraFragment) requireActivity()
                                .getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container);
                        cameraFragment.stopCamera();

                        requireActivity().getSupportFragmentManager().popBackStack();

                        Toast.makeText(requireContext(), "Baseline test completed!",
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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

        // TODO: CALL CAMERA
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
                    saveOtherTestResults(currentTestType);

                    CameraFragment cameraFragment = (CameraFragment) requireActivity()
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                    cameraFragment.stopCamera();

                    requireActivity().getSupportFragmentManager().popBackStack();

                    Toast.makeText(requireContext(), "Dual task test completed!",
                            Toast.LENGTH_SHORT).show();

                    currentTestType = TestType.NONE;
                }
            }
        };
        autoStopHandler.postDelayed(autoStopRunnable, DUAL_TASK_DURATION_MS );
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

        // TODO: CALL CAMERA
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
                    saveOtherTestResults(currentTestType);

                    CameraFragment cameraFragment = (CameraFragment) requireActivity()
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                    cameraFragment.stopCamera();

                    requireActivity().getSupportFragmentManager().popBackStack();

                    Toast.makeText(requireContext(), "Sensory ataxia test completed!",
                            Toast.LENGTH_SHORT).show();

                    currentTestType = TestType.NONE;
                }
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, SENSORY_DURATION_MS ); // 30 seconds = 30000ms
        currentAssessment.setSensoryAtaxiaTestDate(new Date());
    }

    public static void setFrameRate(float fps) {
        frameRate = fps;
    }

    private void saveOtherTestResults(TestType currentTestType) {
        // Calculate metrics (same as baseline
        float currentSpeed = 0.0f;
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
//        float currentBalance = GaitAnalyzer.calculateBalance(, );
        Map<String, Float> ARJScores = new HashMap<>();
        for (String joint : Arrays.asList("left_knee", "right_knee")) {
            float ARJ = AverageRectifiedJerk.calculateAverageRectifiedJerk(
                    angleHistories.get(joint), frameRate
            );
            ARJScores.put(joint, ARJ);
        }
        // TODO: (MANREEN) GUIDELINE: CALL PYTHON CODE WITH THE INSTANCE VARIABLE CONTAINING THE DATA INTO ITS PARAMETER, GET THE RESULT, PASS IT INTO A VARIABLE OR SOMETHING, AND STORE THAT IN CURRENT ASSESSMENT.GETBASELINE().SETBALANCE() OR SETENDURANCE()
        if (!currentAssessment.getBaseline().getARJ().isEmpty()) {
            float baselineARJForKnee = currentAssessment.getBaseline().getARJ().get("knee");
            float stiffnessChangeForKnee = (ARJScores.get("knee") - baselineARJForKnee) / baselineARJForKnee * 100;
            if (currentTestType == TestType.SENSORY_ATAXIA) {
                comparisons.setSensoryAtaxiaARJChange(stiffnessChangeForKnee);
            } else {
                comparisons.setDualTaskARJChange(stiffnessChangeForKnee);
            }
        }

//        if (!currentAssessment.getBaseline().getAsymmetry().isEmpty()) {
//            float baselineStiffnessForKnee = currentAssessment.getBaseline().getStiffness().get("knee");
//            float stiffnessChangeForKnee = (ARJScores.get("knee") - baselineStiffnessForKnee) / baselineStiffnessForKnee * 100;
//            if (currentTestType == TestType.SENSORY_ATAXIA) {
//                comparisons.setSensoryAtaxiaStiffnessChange(stiffnessChangeForKnee);
//            } else {
//                comparisons.setDualTaskStiffnessChange(stiffnessChangeForKnee);
//            }
//        }

        // Save to currentAssessment's SENSORY_ATAXIA & DUAL_TASK section. IS THIS NECESSARY?
//        if (currentTestType == TestType.SENSORY_ATAXIA) {
//            currentAssessment.getSensoryAtaxia().setWalkingSpeed(currentSpeed);
//            currentAssessment.getSensoryAtaxia().setStiffness(ARJScores);
//            currentAssessment.getSensoryAtaxia().setAsymmetry(asymmetryScores);
//        } else {
//            currentAssessment.getDualTask().setWalkingSpeed(currentSpeed);
//            currentAssessment.getDualTask().setStiffness(ARJScores);
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
        if (angleHistories != null) {
            angleHistories.clear();
        }

        //TODO: (JENNY) ADD THE CURRENTASSESSMENT OBJEC TO INTERNAL STORAGE

//        currentAssessment.getBaseline().setWalkingSpeed(currentSpeed);
//        // TODO: ADD METHODS SET BALANCE AND SET ENDURANCE IN ASSESSMENT DATA (MANREEN)
//        currentAssessment.getBaseline().setARJ(ARJScores);
//        currentAssessment.setStatus(AssessmentData.AssessmentStatus.BASELINE_COMPLETED);
//        currentAssessment.setBaselineTestDate(new Date());
//
//        // Call DataStorageManager to save as a JSON file
//        DataStorageManager.saveAssessment(requireContext(), currentAssessment);

    }

    // TODO: baseline currently only saves ARJ. It does not save speed, balance, or endurance (code needed from Manreen)
    private void saveBaselineResults() throws IOException {
        // Calculation speed, ARJ, asymmetry
        float currentSpeed = 0.0f;
        Map<String, Float> ARJScores = new HashMap<>();
        for (String joint : Arrays.asList("left_knee", "right_knee")) {
            float arj = AverageRectifiedJerk.calculateAverageRectifiedJerk(
                    angleHistories.get(joint), frameRate
            );
            ARJScores.put(joint, arj);
        }

        Toast.makeText(requireContext(), "Baseline recorded!", Toast.LENGTH_SHORT).show();

        // Save to AssessmentData object currentAssessment
        currentAssessment.getBaseline().setWalkingSpeed(currentSpeed);
        // TODO: ADD METHODS SET BALANCE AND SET ENDURANCE IN ASSESSMENT DATA (MANREEN)
        currentAssessment.getBaseline().setARJ(ARJScores);
        currentAssessment.setStatus(AssessmentData.AssessmentStatus.BASELINE_COMPLETED);
        currentAssessment.setBaselineTestDate(new Date());

        // Call DataStorageManager to save as a JSON file
        DataStorageManager.saveAssessment(requireContext(), currentAssessment);

        if (angleHistories != null) {
            angleHistories.clear();
        }

        Toast.makeText(requireContext(), "Baseline test saved. Next test available tomorrow.", Toast.LENGTH_LONG).show();
    }

}