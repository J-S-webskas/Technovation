package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class BalanceChart extends BiomechanicalChart {
    // Measures postural stability
    // Normal: 50-150 mm²/s, >300 = poor balance

    public BalanceChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        super(id, title, "Degrees", historicalData, condition);
    }

//    public String getTitle() { return title; }
    @Override public String getUnit() { return unit; }

    // TODO: Need doctors to verify that this measurement is correct
    // TODO: I need the code for balance in order to implement this method
    @Override public String getCondition() { return condition; }

}