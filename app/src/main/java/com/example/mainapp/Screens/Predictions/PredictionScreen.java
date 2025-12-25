package com.example.mainapp.Screens.Predictions;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mainapp.R;

public class PredictionScreen extends AppCompatActivity {

    private CardView cardGamePrediction;
    private CardView cardManualPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_screen);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardGamePrediction = findViewById(R.id.cardGamePrediction);
        cardManualPrediction = findViewById(R.id.cardManualPrediction);
    }

    private void setupClickListeners() {
        cardGamePrediction.setOnClickListener(v -> {
            Intent intent = new Intent(PredictionScreen.this, GamePrediction.class);
            startActivity(intent);
        });

        cardManualPrediction.setOnClickListener(v -> {
            Intent intent = new Intent(PredictionScreen.this, ManualPrediction.class);
            startActivity(intent);
        });
    }
}