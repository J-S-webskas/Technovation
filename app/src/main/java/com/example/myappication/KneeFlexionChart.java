package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class KneeFlexionChart extends BiomechanicalChart {
    // Knee flexion angle during swing phase
    // Normal: 50-70 degrees, <40 = stiff knee gait

    public KneeFlexionChart(int id, Map<Date, Float> historicalData, String condition) {
        super(id, "Knee Flexion", "°", historicalData, condition);
    }

    @Override public String getTitle() { return "Knee Flexion Angle"; }
    @Override public String getUnit() { return "°"; }
    @Override public String getCondition() { return condition; }

//    @Override
//    public List<Entry> getDataForTimeRange(String timeRange) {
//        return SampleDataHelper.sampleData(historicalData, timeRange);
//    }
}
