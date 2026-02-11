package com.example.myappication;

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.*;
// CORE ANALYZER CODE

public class GaitAnalyzer {

    public static float calculateAverageJerk(ArrayDeque<Float> angleHistory,
                                                 String jointName, float frameRate) {  // movement smoothness: jerk
        if (angleHistory.size() < 20) {
            return 0f;
        }
        float dt = 1.0f / frameRate;  // seconds per frame
        List<Float> angles = new ArrayList<>(angleHistory);

        // 1. Calculate angular velocity (degrees per second)
        List<Float> velocities = new ArrayList<>();
        for (int i = 1; i < angles.size(); i++) {
            float vel = (angles.get(i) - angles.get(i-1)) / dt;
            velocities.add(vel);
        }

        // 2. Calculate jerk (change in acceleration)
        List<Float> accelerations = new ArrayList<>();
        for (int i = 1; i < velocities.size(); i++) {
            float a = (velocities.get(i) - velocities.get(i-1)) / dt;
            accelerations.add(a);
        }
        
        List<Float> jerks = new ArrayList<>();
        for (int i = 1; i < accelerations.size(); i++) {
            float j = (accelerations.get(i) - accelerations.get(i-1)) / dt;
            jerks.add(j);
        }

        // Get joint type from name (e.g., "left_knee" -> "knee")
        String jointType = jointName.contains("_") ?
                jointName.split("_")[1] : jointName;

        if (jerks.isEmpty()) {
            return 0f;  // Units: °/s³
        }
        return calculateMean(jerks);
    }

    // Calculate angle between three points
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

    // Helper: Calculate mean of list
    private static float calculateMean(List<Float> values) {
        float sum = 0;
        for (float val : values) {
            sum += val;
        }
        return sum / values.size();
    }
}
