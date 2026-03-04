package com.example.bioms1;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

public class ReportFragment extends Fragment {

    private RecyclerView recyclerView;      // The container for cards
    private ReportAdapter adapter;          // Connects data to cards
    private List<Report> reportList;        // Stores all reports
    private Button btnCreateReport;          // Button to make new reports

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // 1. Set up the screen layout
        // 2. Initialize the RecyclerView
        // 3. Create sample reports
        // 4. Connect adapter to RecyclerView
        // 5. Set up button click listener

        Button btnBaseline = view.findViewById(R.id.createNewReport);

        btnBaseline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate new report...
            }
        });

        return view;
    }

    private void createNewReport() {
        // 1. Get input from user (maybe open a dialog/form)
        // 2. Create a new Report object with the data
        // 3. Add it to the list
        // 4. Tell adapter to show the new card
    }
}