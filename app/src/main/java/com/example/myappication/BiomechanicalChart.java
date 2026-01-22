package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BiomechanicalChart {

    protected int id;
    protected String title;
    protected String unit;
    protected String condition;
    protected Map<Date, Float> baselineData;
    protected Map<Date, Float> dualTaskData;
    protected Map<Date, Float> sensoryAtaxiaData;

    public BiomechanicalChart(int id, String title, String unit,
                              Map<Date, Float> baselineData, String condition) {
        this.id = id;
        this.title = title;
        this.unit = unit;
        this.baselineData = baselineData != null? baselineData : new HashMap<>();
        this.condition = condition;
    }

    // Common methods that all biomarkers will have
    public String getTitle() {
        return title;
    }

    public abstract String getUnit();
    public abstract String getCondition();

    // Method to get filtered data for a time range
    public List<Entry> getDataForTimeRange(String timeRange) {
        List<Entry> filteredEntries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        switch (timeRange) {
            case "1month":
                cal.setTime(now);
                cal.add(Calendar.MONTH, -1);
                Date oneMonthAgo = cal.getTime();
                // Get 4-5 evenly spaced points from last month
                filteredEntries = getSampledData(oneMonthAgo, now, 5);
                break;

            case "6month":
                cal.setTime(now);
                cal.add(Calendar.MONTH, -6);
                Date sixMonthsAgo = cal.getTime();
                // Get 4-5 points from last 6 months
                filteredEntries = getSampledData(sixMonthsAgo, now, 5);
                break;

            case "1year":
                cal.setTime(now);
                cal.add(Calendar.YEAR, -1);
                Date oneYearAgo = cal.getTime();
                // Get 4-5 points from last year
                filteredEntries = getSampledData(oneYearAgo, now, 5);
                break;

            case "max":
                // Get all available data (but limit to reasonable number)
                filteredEntries = getAllData(15); // Max 15 points
                break;
        }

        return filteredEntries;
    }
    // 1. Filter dates to only keep dates within the time range
    // 2. Sort dates chronologically
    // 3. Sample evenly if too many points
    // 4. Convert dates to "days since start" for X-axis
    protected  List<Entry> getSampledData(Date startDate, Date endDate, int maxPoints) {
        List<Entry> entries = new ArrayList<>();
        List<Date> datesInRange = new ArrayList<>();

        // Filter dates in range
        for (Map.Entry<Date, Float> entry : baselineData.entrySet()) {
            if (!entry.getKey().before(startDate) && !entry.getKey().after(endDate)) {
                datesInRange.add(entry.getKey());
            }
        }

        // Sort dates
        Collections.sort(datesInRange);

        // Sample evenly
        if (datesInRange.size() <= maxPoints) {
            // Use all points
            for (Date date : datesInRange) {
                float daysSinceStart = getDaysBetween(startDate, date);
                entries.add(new Entry(daysSinceStart, baselineData.get(date)));
            }
        } else {
            // Sample evenly
            int step = datesInRange.size() / maxPoints; //Step size is the gap between each selected dates
            for (int i = 0; i < maxPoints; i++) {
                Date date = datesInRange.get(i * step);
                float daysSinceStart = getDaysBetween(startDate, date);
                entries.add(new Entry(daysSinceStart, baselineData.get(date)));
            }
        }

        return entries;
    }
    protected  List<Entry> getAllData(int maxPoints) {
        List<Entry> entries = new ArrayList<>();
        List<Date> allDates = new ArrayList<>(baselineData.keySet());

        // Sort dates (ascending)
        Collections.sort(allDates);

        // Sample if too many points
        if (allDates.size() <= maxPoints) {
            // Use all points
            for (Date date : allDates) {
                float daysSinceEarliest = getDaysBetween(allDates.get(0), date);
                entries.add(new Entry(daysSinceEarliest, baselineData.get(date)));
            }
        } else {
            // Sample evenly
            int step = allDates.size() / maxPoints;
            for (int i = 0; i < maxPoints; i++) {
                int index = i * step;
                if (index < allDates.size()) {
                    Date date = allDates.get(index);
                    float daysSinceEarliest = getDaysBetween(allDates.get(0), date);
                    entries.add(new Entry(daysSinceEarliest, baselineData.get(date)));
                }
            }
        }

        return entries;
    }

//    public Map<String, List<Entry>>  getAllDataSeries(String timeRange) {
//        Map<String, List<Entry>> series = new LinkedHashMap<>();
//        String baseline = getTitle();
//        series.put(baseline, getDataForTimeRange(timeRange));
//
//        series.put(baseline + "DualTask", getSamp)
//    }

    protected float getDaysBetween(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        return (float) (diff / (1000.0 * 60 * 60 * 24)); // Convert milliseconds to days
    }
    // Finds most recent date in data (latest that user recorded their data)
    public Date getLatestDate() {
        // Has to have some data
        if (baselineData == null || baselineData.isEmpty()) {
            return new Date();
        }

        // If anotherDate is AFTER someDate → TRUE → latest = anotherDate
        // If anotherDate is BEFORE/EQUAL someDate → FALSE → latest unchanged
        Date latest = null;
        for (Date date : baselineData.keySet()) {
            if (latest == null || date.after(latest)) {
                latest = date;
            }
        }
        return latest != null ? latest : new Date();
    }


    public String getFormattedLatestDate() {
        Date latest = getLatestDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(latest);
    }

    // Calculates mean of Y-values
    public float getAverageForRange(String timeRange) {
        List<Entry> entries = getDataForTimeRange(timeRange);
        if (entries.isEmpty()) return 0f;

        float sum = 0;
        for (Entry entry : entries) {
            sum += entry.getY();
        }
        return sum / entries.size();
    }

    // Finds maximum Y-value
    public float getMaxForRange(String timeRange) {
        List<Entry> entries = getDataForTimeRange(timeRange);
        if (entries.isEmpty()) return 0f;

        float max = Float.MIN_VALUE;
        for (Entry entry : entries) {
            if (entry.getY() > max) max = entry.getY();
        }
        return max;
    }
    public float getCurrentValue() {
        Date latest = getLatestDate();
        return baselineData.getOrDefault(latest, 0f);
    }
}