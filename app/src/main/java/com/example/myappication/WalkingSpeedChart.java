package com.example.myappication;

import java.util.Date;
import java.util.Map;

public class WalkingSpeedChart extends BiomechanicalChart {
    // Knee flexion angle during swing phase
    // Normal: 50-70 degrees, <40 = stiff knee gait

    public WalkingSpeedChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        super(id, title, "m/s", historicalData, condition);
    }

//    public String getTitle() { return title; }
    @Override public String getUnit() { return unit; }

    // TODO: Need doctors to verify that this measurement is correct
    // TODO: need to change this method: walking speed implementation (assessed using pixels) to GPS
    public String getCondition() {
        float speedMps = getCurrentValue();  // meters per second from Python

        // Clinical walking speed classifications (for neurological conditions):
        // Based on typical values from your Python code's pixel/sec calculations
        // Converted to approximate m/s (assuming camera calibration)

        if (speedMps >= 1.2) return "Excellent";
        if (speedMps >= 1.0) return "Good";
        if (speedMps >= 0.8) return "Fair";
        if (speedMps >= 0.6) return "Poor";
        return "Critical";
    }

    // For dual-task impact assessment (Python DualTest)
    public String getDualTaskImpact(float baselineSpeed, float dualTaskSpeed) {
        float speedChangePercent = ((dualTaskSpeed - baselineSpeed) / baselineSpeed) * 100;
        float speedImpact = Math.abs(speedChangePercent);

        // Based on your Python dual_task_impact scoring
        if (speedImpact > 50) return "Large dual-task deficit";
        if (speedImpact > 30) return "Moderate dual-task deficit";
        if (speedImpact > 15) return "Mild dual-task deficit";
        if (speedImpact > 5) return "Minimal dual-task impact";
        return "No significant dual-task impact";
    }

}
