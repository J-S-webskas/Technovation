package com.example.myappication;

import com.github.mikephil.charting.components.AxisBase;

import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DayAxisValueFormatter extends ValueFormatter {
    private final SimpleDateFormat dateFormat;
    private final Date currentDate = new Date();

    public DayAxisValueFormatter() {
        this.dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        // value represents days offset from current date
        int daysAgo = (int) value;

        // Calculate the date for this data point
        Date pointDate = new Date(currentDate.getTime() - (daysAgo * 24 * 60 * 60 * 1000L));

        // For day view, show date like "Dec 24"
        return dateFormat.format(pointDate);
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return getFormattedValue(value);
    }
}