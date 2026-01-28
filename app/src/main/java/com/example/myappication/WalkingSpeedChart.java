package com.example.myappication;

import java.util.Date;
import java.util.Map;

public class WalkingSpeedChart extends BiomechanicalChart {
    // Knee flexion angle during swing phase
    // Normal: 50-70 degrees, <40 = stiff knee gait

    public WalkingSpeedChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        super(id, title, "m/s", historicalData);
    }

//    public String getTitle() { return title; }
    @Override public String getUnit() { return unit; }

    private float getCurrentValue() {
        return 0;
    }

//    // For dual-task impact assessment (Python DualTest)
//    public String getDualTaskImpact(float baselineSpeed, float dualTaskSpeed) {
//        float speedChangePercent = ((dualTaskSpeed - baselineSpeed) / baselineSpeed) * 100;
//        float speedImpact = Math.abs(speedChangePercent);
//
//        // Based on your Python dual_task_impact scoring
//        if (speedImpact > 50) return "Large dual-task deficit";
//        if (speedImpact > 30) return "Moderate dual-task deficit";
//        if (speedImpact > 15) return "Mild dual-task deficit";
//        if (speedImpact > 5) return "Minimal dual-task impact";
//        return "No significant dual-task impact";
//    }

}
