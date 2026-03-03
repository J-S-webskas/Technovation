package com.example.myappication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class demoSession extends AppCompatActivity {

    private Button recordButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_session);

        recordButton = findViewById(R.id.record_button);
        cancelButton = findViewById(R.id.cancel_button);

        // When Cancel is clicked → go back to MainActivity
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(demoSession.this, MainActivity.class);
                startActivity(intent);
                finish(); // closes demoSession
            }
        });

        // When Record is clicked → open MainActivity and load RecordFragment
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(demoSession.this, MainActivity.class);
                intent.putExtra("openFragment", "record");
                startActivity(intent);
                finish(); // closes demoSession
            }
        });
    }
}