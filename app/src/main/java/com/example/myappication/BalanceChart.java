package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class BalanceChart extends BiomechanicalChart {
    // Measures postural stability
    // Normal: 50-150 mm²/s, >300 = poor balance

    public BalanceChart(int id, Map<Date, Float> historicalData, String condition) {
        super(id, "Balance (COP)", "mm²/s", historicalData, condition);
    }

    @Override public String getTitle() { return "Balance Control"; }
    @Override public String getUnit() { return "mm²/s"; }
    @Override public String getCondition() { return condition; }

//    @Override
//    public List<Entry> getDataForTimeRange(String timeRange) {
//        // Filter and sample data similar to PelvicSwayChart
//        return SampleDataHelper.sampleData(historicalData, timeRange);
//    }
}