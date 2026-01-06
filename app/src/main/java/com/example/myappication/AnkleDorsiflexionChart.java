package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AnkleDorsiflexionChart extends BiomechanicalChart {
    // Ankle dorsiflexion range of motion
    // Normal: 10-20 degrees, <5 = limited DF

    public AnkleDorsiflexionChart(int id, Map<Date, Float> historicalData, String condition) {
        super(id, "Ankle Dorsiflexion", "°", historicalData, condition);
    }

    @Override public String getTitle() { return "Ankle Mobility"; }
    @Override public String getUnit() { return "°"; }
    @Override public String getCondition() { return condition; }

//    @Override
//    public List<Entry> getDataForTimeRange(String timeRange) {
//        return SampleDataHelper.sampleData(historicalData, timeRange);
//    }
}