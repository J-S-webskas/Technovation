package com.example.myappication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.ChartData;

import java.util.List;

public class PelvicSwayAdapter extends RecyclerView.Adapter<ChartCardViewHolder> {
    private List<PelvicSwayChart> charts;
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION; // No selection initially
    private OnCardClickListener clickListener;
    private String timeRange;

    public interface OnCardClickListener {
        void onCardClick(int position, ChartData data);
    }
//     Initializes with chart data and initial time range
    public PelvicSwayAdapter(List<PelvicSwayChart> charts, String timeRange, Context context) {
        this.context = context;
        this.charts = charts;
        this.timeRange = timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    @Override
    public void onBindViewHolder(@NonNull ChartCardViewHolder holder, int position) {
        PelvicSwayChart chart = charts.get(position);

        holder.bind(chart, timeRange, null);

        String chartTitle = chart.getTitle();

        // Set click listener ONLY for PelvicSway cards
        if (chartTitle != null && chartTitle.equalsIgnoreCase("Pelvic Sway")) {
            holder.cardView.setOnClickListener(v -> {
                // Use holder.getAdapterPosition() instead of position
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    openPelvicSwayDetail(charts.get(adapterPosition), adapterPosition);
                }
            });
            holder.cardView.setClickable(true);
        } else {
            holder.cardView.setOnClickListener(null);
            holder.cardView.setClickable(false);
        }
    }

    private void openPelvicSwayDetail(PelvicSwayChart chart, int position) {
        if (!(context instanceof FragmentActivity)) return;

        FragmentActivity activity = (FragmentActivity) context;

        PelvicSwayDetailFragment fragment = new PelvicSwayDetailFragment();

        // Create Bundle with arguments
        Bundle args = new Bundle();
        args.putString("chart_title", chart.getTitle());
        // Add other data your fragment needs
        fragment.setArguments(args);

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("pelvic_sway_detail")
                .commit();
    }

    public void setOnItemClickListener(OnCardClickListener listener) {
        this.clickListener = listener;
    }

    public void updateData(List<PelvicSwayChart> newCharts) {
        this.charts = newCharts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChartCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart_card, parent, false);
        return new ChartCardViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return charts.size();
    }
}