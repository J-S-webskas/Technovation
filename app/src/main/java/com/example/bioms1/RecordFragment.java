package com.example.myappication;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.example.myappication.Speed.currentSpeed;
import static com.example.myappication.Speed.totalDistance;

import android.content.Intent;
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

public class RecordFragment extends Fragment {

    private Handler autoStopHandler;
    private Runnable autoStopRunnable;

    private static final int BASELINE_DURATION_MS = 15000;
    private static final int DUAL_TASK_DURATION_MS = 15000;
    private static final int SENSORY_DURATION_MS = 15000;

    private String username = "123";
    private static float frameRate = 0;

    private AssessmentData currentAssessment = new AssessmentData(username);
    private Calendar baselineTestDate;

    private static Map<String, ArrayDeque<Float>> angleHistories = new HashMap<>();
    private static Map<String, ArrayDeque<Float>> BalanceHistories = new HashMap<>();

    private enum TestType { NONE, BASELINE, DUAL_TASK, SENSORY_ATAXIA }
    private TestType currentTestType = TestType.NONE;

    private Button howTo;

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

        btnSpeed.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Speed.class);
            startActivity(intent);
        });

        howTo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), demoSession.class);
            startActivity(intent);
        });
    }

    public static void HipBalanceaddAnglePerFrame(String name, float hip_angle) {

        ArrayDeque<Float> HipAngles = BalanceHistories.get(name);

        if (HipAngles == null) {
            HipAngles = new ArrayDeque<>();
            BalanceHistories.put(name, HipAngles);
        }

        HipAngles.add(hip_angle);
    }

    public static void KneeBalanceaddAnglePerFrame(String name, float knee_angle) {

        ArrayDeque<Float> KneeAngles = BalanceHistories.get(name);

        if (KneeAngles == null) {
            KneeAngles = new ArrayDeque<>();
            BalanceHistories.put(name, KneeAngles);
        }

        KneeAngles.add(knee_angle);
    }

    public static void addAnglePerFrame(String name, float angle) {

        ArrayDeque<Float> angles = angleHistories.get(name);

        if (angles == null) {
            angles = new ArrayDeque<>();
            angleHistories.put(name, angles);
        }

        angles.add(angle);
    }

    public void stopCurrentTestAndDiscardData() {

        if (currentTestType == TestType.NONE) {
            Toast.makeText(requireContext(), "No test running", Toast.LENGTH_SHORT).show();
            return;
        }

        if (autoStopHandler != null && autoStopRunnable != null) {
            autoStopHandler.removeCallbacks(autoStopRunnable);
            autoStopHandler = null;
            autoStopRunnable = null;
        }

        CameraFragment cameraFragment = (CameraFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        cameraFragment.stopCamera();

        TestType cancelledTest = currentTestType;
        currentTestType = TestType.NONE;
        activeTest = null;

        angleHistories.clear();
        BalanceHistories.clear();

        requireActivity().getSupportFragmentManager().popBackStack();

        Toast.makeText(requireContext(),
                getTestName(cancelledTest) + " test cancelled - results not saved",
                Toast.LENGTH_SHORT).show();
    }

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

        if (currentTestType != TestType.NONE) {
            stopCurrentTestAndDiscardData();
            return;
        }

        if (!currentAssessment.canTakeBaselineTest()) {
            Toast.makeText(requireContext(),
                    "Baseline test was already completed for today.",
                    Toast.LENGTH_SHORT).show();
        }

        currentTestType = TestType.BASELINE;
        baselineTestDate = Calendar.getInstance();

        Toast.makeText(requireContext(),
                "Baseline test started - Walk normally",
                Toast.LENGTH_SHORT).show();

        autoStopHandler = new Handler(Looper.getMainLooper());

        autoStopRunnable = () -> {

            if (currentTestType == TestType.BASELINE) {

                try {

                    saveBaselineResults();

                    CameraFragment cameraFragment = (CameraFragment) requireActivity()
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);

                    cameraFragment.stopCamera();

                    requireActivity().getSupportFragmentManager().popBackStack();

                    Toast.makeText(requireContext(),
                            "Baseline test completed!",
                            Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                currentTestType = TestType.NONE;
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, BASELINE_DURATION_MS);
    }

    private void startDualTaskTest() {

        if (currentTestType != TestType.NONE) {
            stopCurrentTestAndDiscardData();
            return;
        }

        if (baselineResult == null) {
            Toast.makeText(requireContext(),
                    "Record baseline first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        activeTest = new DualTaskTest(baselineResult);
        activeTest.startTest();

        currentTestType = TestType.DUAL_TASK;

        Toast.makeText(requireContext(),
                "Dual task test started - Count backwards from 30",
                Toast.LENGTH_SHORT).show();

        autoStopHandler = new Handler(Looper.getMainLooper());

        autoStopRunnable = () -> {

            if (currentTestType == TestType.DUAL_TASK) {

                saveOtherTestResults(currentTestType);

                CameraFragment cameraFragment = (CameraFragment) requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                cameraFragment.stopCamera();

                requireActivity().getSupportFragmentManager().popBackStack();

                Toast.makeText(requireContext(),
                        "Dual task test completed!",
                        Toast.LENGTH_SHORT).show();

                currentTestType = TestType.NONE;
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, DUAL_TASK_DURATION_MS);
    }

    private void startSensoryAtaxiaTest() {

        if (currentTestType != TestType.NONE) {
            stopCurrentTestAndDiscardData();
            return;
        }

        if (baselineResult == null) {
            Toast.makeText(requireContext(),
                    "Record baseline test first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        activeTest = new DualTaskTest(baselineResult);
        activeTest.startTest();

        currentTestType = TestType.SENSORY_ATAXIA;

        Toast.makeText(requireContext(),
                "Sensory ataxia test started - Close your eyes and walk",
                Toast.LENGTH_SHORT).show();

        autoStopHandler = new Handler(Looper.getMainLooper());

        autoStopRunnable = () -> {

            if (currentTestType == TestType.SENSORY_ATAXIA) {

                saveOtherTestResults(currentTestType);

                CameraFragment cameraFragment = (CameraFragment) requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                cameraFragment.stopCamera();

                requireActivity().getSupportFragmentManager().popBackStack();

                Toast.makeText(requireContext(),
                        "Sensory ataxia test completed!",
                        Toast.LENGTH_SHORT).show();

                currentTestType = TestType.NONE;
            }
        };

        autoStopHandler.postDelayed(autoStopRunnable, SENSORY_DURATION_MS);

        currentAssessment.setSensoryAtaxiaTestDate(new Date());
    }

    public static void setFrameRate(float fps) {
        frameRate = fps;
    }

    private void saveOtherTestResults(TestType currentTestType) {

        TestComparisons comparisons = currentAssessment.getComparisons();

        float baselineSpeed = currentAssessment.getBaseline().getWalkingSpeed();

        if (baselineSpeed > 0) {

            float speedChange = ((Speed.currentSpeed - baselineSpeed) / baselineSpeed) * 100f;

            if (currentTestType == TestType.SENSORY_ATAXIA)
                comparisons.setSensoryAtaxiaSpeedChange(speedChange);
            else
                comparisons.setDualTaskSpeedChange(speedChange);
        }

        float balance = BalanceCalculator.calculateOffBalance(
                BalanceHistories.get("left_hip"),
                BalanceHistories.get("left_knee"));

        float baselineBalance = currentAssessment.getBaseline().getBalanceScore();

        if (baselineBalance > 0) {

            float balanceChange = ((balance - baselineBalance) / baselineBalance) * 100f;

            if (currentTestType == TestType.SENSORY_ATAXIA)
                comparisons.setSensoryAtaxiaBalanceChange(balanceChange);
            else
                comparisons.setDualTaskBalanceChange(balanceChange);
        }

        float baselineEndurance = currentAssessment.getBaseline().getEnduranceScore();

        if (baselineEndurance > 0) {

            float enduranceChange =
                    ((Speed.totalDistance - baselineEndurance) / baselineEndurance) * 100f;

            if (currentTestType == TestType.SENSORY_ATAXIA)
                comparisons.setSensoryAtaxiaEnduranceChange(enduranceChange);
            else
                comparisons.setDualTaskEnduranceChange(enduranceChange);
        }

        angleHistories.clear();
        BalanceHistories.clear();
    }

    private void saveBaselineResults() throws IOException {

        float speed = Speed.currentSpeed;
        float endurance = Speed.totalDistance;

        float balance = BalanceCalculator.calculateOffBalance(
                BalanceHistories.get("left_hip"),
                BalanceHistories.get("left_knee"));

        Map<String, Float> ARJScores = new HashMap<>();

        for (String joint : Arrays.asList("left_knee", "right_knee")) {

            float arj = AverageRectifiedJerk.calculateAverageRectifiedJerk(
                    angleHistories.get(joint), frameRate);

            ARJScores.put(joint, arj);
        }

        currentAssessment.getBaseline().setWalkingSpeed(speed);
        currentAssessment.getBaseline().setEnduranceScore(endurance);
        currentAssessment.getBaseline().setBalanceScore(balance);
        currentAssessment.getBaseline().setARJ(ARJScores);

        currentAssessment.setStatus(AssessmentData.AssessmentStatus.BASELINE_COMPLETED);
        currentAssessment.setBaselineTestDate(new Date());

        DataStorageManager.saveAssessment(requireContext(), currentAssessment);

        angleHistories.clear();
        BalanceHistories.clear();

        Toast.makeText(requireContext(),
                "Baseline test saved. Next test available tomorrow.",
                Toast.LENGTH_LONG).show();
    }
}
