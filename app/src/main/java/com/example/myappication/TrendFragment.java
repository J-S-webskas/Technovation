package com.example.myappication;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrendFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaterialButtonToggleGroup toggleGroup;
    private PelvicSwayAdapter adapter;
    private String currentTimeRange = "1month"; // Default make sure it's popping red


    public TrendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_trend, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        toggleGroup = view.findViewById(R.id.toggleGroup);

        // Uses requireContext() instead of this (Fragments need context from parent Activity)
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create ONE Pelvic Sway chart with ALL historical data
        PelvicSwayChart pelvicSway = createPelvicSwayChart();
        List<PelvicSwayChart> charts = new ArrayList<>();
        charts.add(pelvicSway);

        // Connects adapter to recyclerView
        adapter = new PelvicSwayAdapter(charts, currentTimeRange, getContext());
        recyclerView.setAdapter(adapter);

        // Time range selection
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.oneMonth) {
                    currentTimeRange = "1month";
                    Toast.makeText(requireContext(), "Showing 1-month trend", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.sixMonth) {
                    currentTimeRange = "6month";
                    Toast.makeText(requireContext(), "Showing 6-month trend", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.oneYear) {
                    currentTimeRange = "1year";
                    Toast.makeText(requireContext(), "Showing 1-year trend", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.MAX) {
                    currentTimeRange = "max";
                    Toast.makeText(requireContext(), "Showing full history", Toast.LENGTH_SHORT).show();
                }

                // Update adapter with new time range
                adapter.setTimeRange(currentTimeRange);
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private PelvicSwayChart createPelvicSwayChart() {
        // Create map of historical data (Date -> Value)
        Map<Date, Float> historicalData = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        Random rand = new Random();

        // Generate 2 years of stimulated daily data
        for (int i = 0; i < 730; i++) { // 730 days = ~2 years
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date date = cal.getTime();

            // Simulate pelvic sway values with some trend
            float baseValue = 5 + (i / 100f); // Slowly increasing trend
            float randomVariation = rand.nextFloat() * 3;
            float value = baseValue + randomVariation;

            historicalData.put(date, value);
        }

        return new PelvicSwayChart(1, "Pelvic Sway", historicalData, "Elevated");
    }
}