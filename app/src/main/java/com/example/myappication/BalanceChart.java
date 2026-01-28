package com.example.myappication;

import java.util.Date;
import java.util.Map;

public class BalanceChart extends BiomechanicalChart {

    public BalanceChart(int id, String title, Map<Date, Float> historicalData) {
        super(id, title, "Degrees", historicalData);
    }

//    public String getTitle() { return title; }
    @Override public String getUnit() { return unit; }

}