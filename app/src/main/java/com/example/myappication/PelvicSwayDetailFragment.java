package com.example.myappication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.List;

public class PelvicSwayDetailFragment extends Fragment {

    // UI Components
    private TextView chartTitleTextView;
    private TextView timeRangeTextView;
    private TextView chartDescription;
    private LineChart areaChart;
    private String chartTitle;
    private String timeRange;
    private int position;

    public PelvicSwayDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_pelvic_sway_detail, container, false);

        // Initialize UI components
        initializeViews(view);

        // Get data from Bundle (passed from adapter)
        getDataFromBundle();

        // Setup UI with data
        setupUI();

        // Setup chart
        setupChart();

        return view;
    }

    private void initializeViews(View view) {
        chartTitleTextView = view.findViewById(R.id.detailTitle);
        timeRangeTextView = view.findViewById(R.id.timeRangeText);
        chartDescription = view.findViewById(R.id.chartDescription);
        areaChart = view.findViewById(R.id.areaChart);
    }

    private void getDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            chartTitle = args.getString("chart_title", "Pelvic Sway");
            timeRange = args.getString("time_range", "Last 30 days");
            position = args.getInt("position", 0);

            // Optional: Show a toast to confirm data was received
            Toast.makeText(getContext(),
                    "Showing: " + chartTitle + " | Position: " + position,
                    Toast.LENGTH_SHORT).show();
        } else {
            // Default values if no Bundle
            chartTitle = "Pelvic Sway Analysis";
            timeRange = "Last 30 days";
            position = 0;
        }
    }

    private void setupUI() {
        // Update chart title
        if (chartTitleTextView != null) {
            chartTitleTextView.setText(chartTitle + " Analysis");
        }

        // Update time range text
        if (timeRangeTextView != null) {
            timeRangeTextView.setText(timeRange + " | Updated: Today");
        }

        // Update description based on chart type
        if (chartDescription != null) {
            String description = createDescriptionForChart(chartTitle);
            chartDescription.setText(description);
        }
    }

    private void setupChart() {
        // Configure the LineChart
        areaChart.getDescription().setEnabled(false);
        areaChart.setTouchEnabled(true);
        areaChart.setDragEnabled(true);
        areaChart.setScaleEnabled(true);
        areaChart.setDrawGridBackground(false);
        areaChart.setPinchZoom(true);

        // Create sample data for Pelvic Sway chart
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            float value = (float) (Math.sin(i * 0.5) * 10 + 20 + Math.random() * 5);
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Pelvic Sway (degrees)");
        dataSet.setColor(0xFF1ec688); // Green color matching your theme
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(0xFF1ec688);
        dataSet.setCircleRadius(4f);
        dataSet.setFillColor(0x551ec688); // Semi-transparent fill
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        areaChart.setData(lineData);
        areaChart.invalidate(); // Refresh chart
    }

    private String createDescriptionForChart(String chartTitle) {
        // Customize description based on chart type
        if (chartTitle.contains("Pelvic") || chartTitle.contains("Sway")) {
            return "📊 **Pelvic Sway Analysis**\n\n" +
                    "This chart displays pelvic sway measurements over time. Pelvic sway refers to the side-to-side movement of the pelvis during standing or walking.\n\n" +
                    "🔍 **Key Observations:**\n" +
                    "• **Average Sway:** 2.5°\n" +
                    "• **Maximum Sway:** 4.8°\n" +
                    "• **Stability Index:** 0.85 (Good)\n\n" +
                    "💡 **Clinical Significance:**\n" +
                    "Reduced pelvic sway can indicate improved core stability and balance control. The gradient visualization helps identify:\n" +
                    "1. Balance improvement trends\n" +
                    "2. Core muscle engagement patterns\n" +
                    "3. Rehabilitation progress\n" +
                    "4. Asymmetry detection\n\n" +
                    "📈 **Trend Interpretation:**\n" +
                    "- Decreasing sway indicates improved stability\n" +
                    "- Consistent patterns suggest habitual posture\n" +
                    "- Sudden changes may warrant further assessment\n\n" +
                    "⚠️ **When to Consult:**\n" +
                    "Persistent sway >5° or sudden increases in variability may indicate balance issues.\n\n" +
                    "*Data collected from motion sensors during standing assessments.*";
        }

        return "📊 **Chart Analysis**\n\nDetailed analysis will appear here.";
    }
}