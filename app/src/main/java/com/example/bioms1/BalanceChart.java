package com.example.bioms1;

import com.github.mikephil.charting.data.Entry;
import java.util.Date;
import java.util.Map;

public class BalanceChart extends BiomechanicalChart {
    private String condition;

    public BalanceChart(int id, String title, Map<Date, Float> historicalData) {
        super(id, title, "degrees", historicalData);
        this.condition = "No Data";
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Get formatted entries for balance data
     */
    public Entry[] getFormattedEntries(String timeRange) {
        java.util.List<Entry> entries = getDataForTimeRangeWithGaps(baselineData, timeRange);
        return entries.toArray(new Entry[0]);
    }
}