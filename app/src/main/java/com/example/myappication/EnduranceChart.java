package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.*;

/*
Data model for the EnduranceCHart
 */
public class EnduranceChart extends BiomechanicalChart {
    private String title;
    private String condition;  // Elevated, Critical

    // Store ALL historical data, not just a few points
    private Map<Date, Float> historicalData; // Date -> Value mapping

    public EnduranceChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        super(id, title, "score", historicalData, condition);
    }

//    public String getTitle() {
//        return title;
//    }

    @Override
    public String getUnit() {
        return "Sec";
    }

    // TODO: I need the code for endurance in order to implement this method
    // TODO: Need doctors to verify that this measurement is correct
    public String getCondition() {
        float current = getCurrentValue();
        if (current >= 80) return "Excellent";
        if (current >= 60) return "Good";
        if (current >= 40) return "Fair";
        if (current >= 20) return "Poor";
        return "Critical";
    }

    public String getROMEnduranceCondition(float romDegradation) {
        // Range of Motion endurance - how well ROM is maintained over time
        if (romDegradation < 10) return "Excellent ROM maintenance";
        if (romDegradation < 25) return "Good ROM maintenance";
        if (romDegradation < 40) return "Fair ROM maintenance";
        if (romDegradation < 60) return "Poor ROM maintenance";
        return "Severe ROM fatigue";
    }

    public String getVelocityEnduranceCondition(float velocityDegradation) {
        // Velocity endurance - how well speed is maintained
        if (velocityDegradation < 15) return "Excellent speed maintenance";
        if (velocityDegradation < 30) return "Good speed maintenance";
        if (velocityDegradation < 50) return "Fair speed maintenance";
        if (velocityDegradation < 70) return "Poor speed maintenance";
        return "Severe speed fatigue";
    }

}