package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.*;

/*
Data model for the EnduranceCHart
 */
public class EnduranceChart extends BiomechanicalChart {
    private String title;

    // Store ALL historical data, not just a few points
    private Map<Date, Float> historicalData; // Date -> Value mapping

    public EnduranceChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        super(id, title, "score", historicalData);
    }

    // TODO: I need the code for endurance in order to implement this method

}