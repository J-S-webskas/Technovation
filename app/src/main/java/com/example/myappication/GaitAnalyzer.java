package com.example.myappication;

import java.util.*;
// CORE ANALYZER CODE

public class GaitAnalyzer {

    // Calculate muscle stiffness (Java equivalent of Python calculate_muscle_stiffness)
    public static float calculateMuscleStiffness(ArrayDeque<Float> angleHistory,
                                                 String jointName, float frameRate) {
        if (angleHistory.size() < 20) {
            return 0f;
        }

        List<Float> angles = new ArrayList<>(angleHistory);

        // 1. Calculate angular velocity (degrees per second)
        List<Float> velocities = new ArrayList<>();
        for (int i = 1; i < angles.size(); i++) {
            float timeDiff = 1.0f / frameRate;
            float vel = Math.abs(angles.get(i) - angles.get(i-1)) / timeDiff;
            velocities.add(vel);
        }

        // 2. Calculate jerk (change in acceleration)
        List<Float> jerks = new ArrayList<>();
        if (velocities.size() >= 3) {
            for (int i = 1; i < velocities.size(); i++) {
                float jerk = Math.abs(velocities.get(i) - velocities.get(i-1));
                jerks.add(jerk);
            }
        }

        // 3. Calculate range of motion
        float minAngle = Collections.min(angles);
        float maxAngle = Collections.max(angles);
        float rom = maxAngle - minAngle;

        // 4. Normalize scores (0-100)
        Map<String, Float> romRanges = new HashMap<>();
        romRanges.put("hip", 120f);    // degrees
        romRanges.put("knee", 135f);   // degrees
        romRanges.put("ankle", 50f);   // degrees

        // Get joint type from name (e.g., "left_knee" -> "knee")
        String jointType = jointName.contains("_") ?
                jointName.split("_")[1] : jointName;

        // ROM score (0-100, higher = stiffer)
        float maxRom = romRanges.getOrDefault(jointType, 100f);
        float romScore = Math.max(0, Math.min(100, (1 - (rom / maxRom)) * 100));

        // Velocity score (0-100, higher velocity = lower stiffness)
        float velScore = 0;
        if (!velocities.isEmpty()) {
            float avgVel = calculateMean(velocities);
            float typicalVel = jointName.contains("knee") ? 180f : 120f;
            velScore = Math.max(0, Math.min(100, (1 - (avgVel / typicalVel)) * 100));
        }

        // Jerk score (0-100, higher jerk = higher stiffness)
        float jerkScore = 0;
        if (!jerks.isEmpty()) {
            float avgJerk = calculateMean(jerks);
            jerkScore = Math.min(100, avgJerk * 10);  // Scale factor
        }

        // Combined stiffness score (weighted average)
        float stiffness = (romScore * 0.4f) + (velScore * 0.3f) + (jerkScore * 0.3f);

        return Math.min(100, stiffness);  // Cap at 100
    }

    // Calculate asymmetry (Java equivalent of Python calculate_asymmetry)
    public static float calculateAsymmetry(ArrayDeque<Float> leftHistory,
                                           ArrayDeque<Float> rightHistory,
                                           String jointType) {
        if (leftHistory.size() < 10 || rightHistory.size() < 10) {
            return 0f;
        }

        // Get most common angles (median approximation)
        List<Float> leftList = new ArrayList<>(leftHistory);
        List<Float> rightList = new ArrayList<>(rightHistory);

        // Get last 20 values
        int startLeft = Math.max(0, leftList.size() - 20);
        int startRight = Math.max(0, rightList.size() - 20);

        List<Float> leftRecent = leftList.subList(startLeft, leftList.size());
        List<Float> rightRecent = rightList.subList(startRight, rightList.size());

        float leftCommon = calculateMedian(leftRecent);
        float rightCommon = calculateMedian(rightRecent);

        // Calculate asymmetry percentage
        if (leftCommon == 0 && rightCommon == 0) {
            return 0f;
        }

        float avgAngle = (Math.abs(leftCommon) + Math.abs(rightCommon)) / 2;
        float asymmetry = Math.abs(leftCommon - rightCommon) / avgAngle * 100;

        return asymmetry;
    }

    // Calculate angle between three points (Java equivalent of Python get_angle)
    public static float calculateAngle(float[] a, float[] b, float[] c) {
        // Convert to vectors
        float[] ba = {a[0] - b[0], a[1] - b[1]};
        float[] bc = {c[0] - b[0], c[1] - b[1]};

        // Dot product
        float dot = ba[0]*bc[0] + ba[1]*bc[1];

        // Magnitudes
        float magBA = (float)Math.sqrt(ba[0]*ba[0] + ba[1]*ba[1]);
        float magBC = (float)Math.sqrt(bc[0]*bc[0] + bc[1]*bc[1]);

        // Angle in radians, then convert to degrees
        float angleRad = (float)Math.acos(dot / (magBA * magBC));
        float angleDeg = (float)Math.toDegrees(angleRad);

        // Ensure angle is between 0-180
        return Math.abs(angleDeg) <= 180 ? Math.abs(angleDeg) : 360 - Math.abs(angleDeg);
    }

    // Calculate walking speed from hip positions
    public static float calculateWalkingSpeed(List<float[]> hipPositions, float frameRate) {
        if (hipPositions.size() < 2) {
            return 0f;
        }

        float totalDisplacement = 0;
        for (int i = 1; i < hipPositions.size(); i++) {
            float[] prevLeft = {hipPositions.get(i-1)[0], hipPositions.get(i-1)[1]};
            float[] currLeft = {hipPositions.get(i)[0], hipPositions.get(i)[1]};
            float[] prevRight = {hipPositions.get(i-1)[2], hipPositions.get(i-1)[3]};
            float[] currRight = {hipPositions.get(i)[2], hipPositions.get(i)[3]};

            float leftDisp = distance(prevLeft, currLeft);
            float rightDisp = distance(prevRight, currRight);
            totalDisplacement += (leftDisp + rightDisp) / 2;
        }

        float timeElapsed = (hipPositions.size() - 1) / frameRate;
        return timeElapsed > 0 ? totalDisplacement / timeElapsed : 0f;
    }

    // Helper: Calculate distance between two points
    private static float distance(float[] a, float[] b) {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];
        return (float)Math.sqrt(dx*dx + dy*dy);
    }

    // Helper: Calculate mean of list
    private static float calculateMean(List<Float> values) {
        float sum = 0;
        for (float val : values) {
            sum += val;
        }
        return sum / values.size();
    }

    // Helper: Calculate median of list
    private static float calculateMedian(List<Float> values) {
        List<Float> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2;
        } else {
            return sorted.get(size/2);
        }
    }
}