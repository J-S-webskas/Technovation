package com.example.myappication;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;
import java.util.List;

public class ChartCardViewHolder extends RecyclerView.ViewHolder {

    // Stores references to all views in item_chart_card.xml
    TextView chartTitle;
    TextView chartDate;
    TextView averageValue;
    TextView maxValue;
    TextView conditionIndicator;
    LineChart lineChart;
    CardView cardView;
    public ChartCardViewHolder(@NonNull View itemView) {
        super(itemView);
        chartTitle = itemView.findViewById(R.id.chartTitle);
        chartDate = itemView.findViewById(R.id.chartDate);
        averageValue = itemView.findViewById(R.id.averageValue);
        maxValue = itemView.findViewById(R.id.maxValue);
        conditionIndicator = itemView.findViewById(R.id.conditionIndicator);
        lineChart = itemView.findViewById(R.id.lineChart);
        cardView = itemView.findViewById(R.id.pelvicSwayCardView);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
    }

    public void bind(PelvicSwayChart chart, String timeRange, PelvicSwayAdapter.OnCardClickListener listener) {
        // Set basic text
        chartTitle.setText(chart.getTitle());
        chartDate.setText(chart.getFormattedLatestDate());

        // Get data filtered for the selected time range
        List<Entry> chartData = chart.getDataForTimeRange(timeRange);

        // Convert List<Entry> to List<Float> for chart setup. Setup chart with filtered data
        List<Float> floatData = new ArrayList<>();
        for (Entry entry : chartData) {
            floatData.add(entry.getY());
        }

        // Setup chart with the filtered data
        setupGradientAreaChart(lineChart, floatData, chart.getCondition(), timeRange);

        // Update stats based on filtered data
        updateStats(floatData, chart.getCondition());

        // Set condition indicator
        setConditionIndicator(conditionIndicator, chart.getCondition());
    }

    private void setupXAxis(XAxis xAxis, String timeRange) {
        switch (timeRange) {
            case "1month":
                xAxis.setValueFormatter(new DayAxisValueFormatter());
                xAxis.setLabelCount(5, true);
                break;
            case "6month":
                xAxis.setValueFormatter(new WeekAxisValueFormatter());
                xAxis.setLabelCount(6, true);
                break;
            case "1year":
                xAxis.setValueFormatter(new MonthAxisValueFormatter());
                xAxis.setLabelCount(12, true);
                break;
            case "max":
                xAxis.setValueFormatter(new YearAxisValueFormatter());
                xAxis.setLabelCount(5, true);
                break;
        }
    }

    private void updateStats(List<Float> data, String condition) {
        if (data == null || data.isEmpty()) {
            averageValue.setText("Avg: N/A");
            maxValue.setText("Max: N/A");
            return;
        }

        float sum = 0;
        float max = Float.MIN_VALUE;
        for (float value : data) {
            sum += value;
            if (value > max) max = value;
        }

        float avg = sum / data.size();
        averageValue.setText(String.format("Avg: %.1f mm", avg));
        maxValue.setText(String.format("Max: %.1f mm", max));
    }
    private void setupGradientAreaChart(LineChart chart, List<Float> swayData,
                                        String condition, String timeRange) {
        // Clear any existing data
        chart.clear();

        // Create entries from sway data
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < swayData.size(); i++) {
            entries.add(new Entry(i, swayData.get(i)));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Pelvic Sway");

        // Set line color based on condition
        int lineColor = getConditionColor(condition);
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(2f);

        // Enable gradient fill
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(80);

        // Set gradient color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Drawable gradientDrawable = ContextCompat.getDrawable(itemView.getContext(),
                    getGradientDrawable(condition));
            dataSet.setFillDrawable(gradientDrawable);
        } else {
            // Fallback for older versions
            dataSet.setFillColor(lineColor);
            dataSet.setFillAlpha(80);
        }

        // Disable circles for smooth area look
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);

        // Disable values on points
        dataSet.setDrawValues(false);

        // Create line data
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Configure chart appearance
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // Configure X axis
        XAxis xAxis = chart.getXAxis();
        setupXAxis(xAxis, timeRange);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Configure Y axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#33000000"));
        leftAxis.setAxisMinimum(0f); // Start from 0
        leftAxis.setGranularity(10f);

        // Hide right axis
        chart.getAxisRight().setEnabled(false);

        // Disable touch interactions
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        // Refresh chart
        chart.invalidate();
    }

    private void setConditionIndicator(TextView indicator, String condition) {
        switch (condition.toLowerCase()) {
            case "normal":
                indicator.setText("NORMAL");
                indicator.setBackgroundResource(R.drawable.bg_normal_indicator);
                indicator.setTextColor(Color.WHITE);
                break;
            case "elevated":
                indicator.setText("ELEVATED");
                indicator.setBackgroundResource(R.drawable.bg_elevated_indicator);
                indicator.setTextColor(Color.WHITE);
                break;
            case "critical":
                indicator.setText("CRITICAL");
                indicator.setBackgroundResource(R.drawable.bg_critical_indicator);
                indicator.setTextColor(Color.WHITE);
                break;
        }
    }
    private int getConditionColor(String condition) {
        switch (condition.toLowerCase()) {
            case "normal":
                return Color.parseColor("#4CAF50"); // Green
            case "elevated":
                return Color.parseColor("#FF9800"); // Orange
            case "critical":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.parseColor("#2196F3"); // Blue
        }
    }

    private int getGradientDrawable(String condition) {
        switch (condition.toLowerCase()) {
            case "normal":
                return R.drawable.gradient_green;
            case "elevated":
                return R.drawable.gradient_orange;
            case "critical":
                return R.drawable.gradient_red;
            default:
                return R.drawable.gradient_blue;
        }
    }
}