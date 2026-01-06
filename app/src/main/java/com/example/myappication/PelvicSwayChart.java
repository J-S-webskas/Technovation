package com.example.myappication;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.*;

/*
Data model for the PelvicSwayCHart
 */
public class PelvicSwayChart {
    private String title;
    private String condition;  // Elevated, Critical

    // Store ALL historical data, not just a few points
    private Map<Date, Float> historicalData; // Date -> Value mapping

    public PelvicSwayChart(int id, String title, Map<Date, Float> historicalData, String condition) {
        this.title = title;
        this.historicalData = historicalData;
        this.condition = condition;
    }

    public String getTitle() {
        return title;
    }

    public String getCondition() {
        return condition;
    }

    public Map<Date, Float> getHistoricalData() {
        return historicalData;
    }

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
    private List<Entry> getSampledData(Date startDate, Date endDate, int maxPoints) {
        List<Entry> entries = new ArrayList<>();
        List<Date> datesInRange = new ArrayList<>();

        // Filter dates in range
        for (Map.Entry<Date, Float> entry : historicalData.entrySet()) {
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
                entries.add(new Entry(daysSinceStart, historicalData.get(date)));
            }
        } else {
            // Sample evenly
            int step = datesInRange.size() / maxPoints; //Step size is the gap between each selected dates
            for (int i = 0; i < maxPoints; i++) {
                Date date = datesInRange.get(i * step);
                float daysSinceStart = getDaysBetween(startDate, date);
                entries.add(new Entry(daysSinceStart, historicalData.get(date)));
            }
        }

        return entries;
    }
    private List<Entry> getAllData(int maxPoints) {
        List<Entry> entries = new ArrayList<>();
        List<Date> allDates = new ArrayList<>(historicalData.keySet());

        // Sort dates (ascending)
        Collections.sort(allDates);

        // Sample if too many points
        if (allDates.size() <= maxPoints) {
            // Use all points
            for (Date date : allDates) {
                float daysSinceEarliest = getDaysBetween(allDates.get(0), date);
                entries.add(new Entry(daysSinceEarliest, historicalData.get(date)));
            }
        } else {
            // Sample evenly
            int step = allDates.size() / maxPoints;
            for (int i = 0; i < maxPoints; i++) {
                int index = i * step;
                if (index < allDates.size()) {
                    Date date = allDates.get(index);
                    float daysSinceEarliest = getDaysBetween(allDates.get(0), date);
                    entries.add(new Entry(daysSinceEarliest, historicalData.get(date)));
                }
            }
        }

        return entries;
    }

    private float getDaysBetween(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        return (float) (diff / (1000.0 * 60 * 60 * 24)); // Convert milliseconds to days
    }

    // Finds most recent date in data (latest that user recorded their data)
    public Date getLatestDate() {
        // Has to have some data
        if (historicalData == null || historicalData.isEmpty()) {
            return new Date();
        }

        // If anotherDate is AFTER someDate → TRUE → latest = anotherDate
        // If anotherDate is BEFORE/EQUAL someDate → FALSE → latest unchanged
        Date latest = null;
        for (Date date : historicalData.keySet()) {
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
}