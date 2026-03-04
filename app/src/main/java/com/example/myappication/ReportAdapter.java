package com.example.myappication;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder>{

    private List<Report> reportList;// Holding all lists

    // Creates new card views when needed
    public ReportViewHolder onCreateViewHolder() {

        return null;
    }

    // Binds data from a Report object to a card view
    public void onBindViewHolder() {

    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

    }

    // Returns total number of reports
    @Override
    public int getItemCount() {

        return 0;
    }

    // Inner class that represents each card and handles its interactions
    class ReportViewHolder extends RecyclerView.ViewHolder {
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        // Contains references to all views in a card (title, date, buttons, etc.)
        // Handles click events for each card
    }

}
