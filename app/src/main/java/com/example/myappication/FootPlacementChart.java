package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FootPlacementChart extends BiomechanicalChart {
    // Step width variability during gait
    // Normal: 5-15cm variability, >25cm = abnormal

    public FootPlacementChart(int id, Map<Date, Float> historicalData, String condition) {
        super(id, "Foot Placement", "cm", historicalData, condition);
    }

    @Override public String getTitle() { return "Step Variability"; }
    @Override public String getUnit() { return "cm"; }
    @Override public String getCondition() { return condition; }

//    @Override
//    public List<Entry> getDataForTimeRange(String timeRange) {
//        return SampleDataHelper.sampleData(historicalData, timeRange);
//    }
}