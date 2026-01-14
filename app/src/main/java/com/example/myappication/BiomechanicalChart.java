package com.example.myappication;

import com.github.mikephil.charting.data.Entry;
import java.util.*;

public abstract class BiomechanicalChart {
    protected int id;
    protected String title;
    protected String unit;
    protected Map<Date, Float> historicalData;
    protected String condition;

    public BiomechanicalChart(int id, String title, String unit,
                              Map<Date, Float> historicalData, String condition) {
        this.id = id;
        this.title = title;
        this.unit = unit;
        this.historicalData = historicalData;
        this.condition = condition;
    }

    // Common methods that all biomarkers will have
    public abstract String getTitle();
    public abstract String getUnit();
    public abstract String getCondition();
//    public abstract List<Entry> getDataForTimeRange(String timeRange);

    // Common stats methods
    public float getAverageForRange(String timeRange) {
//        List<Entry> entries = getDataForTimeRange(timeRange);
//        if (entries.isEmpty()) return 0f;
//        float sum = 0;
//        for (Entry entry : entries) sum += entry.getY();
//        return sum / entries.size();
        return 0;
    }
}