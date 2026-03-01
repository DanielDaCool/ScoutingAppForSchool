package com.example.mainapp.Screens.Predictions;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mainapp.Adapters.PredictionPagerAdapter;
import com.example.mainapp.R;

public class PredictionScreen extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvHeader, tabGame, tabManual, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_screen);

        tvHeader  = findViewById(R.id.tvHeader);
        tabGame   = findViewById(R.id.tabGame);
        tabManual = findViewById(R.id.tabManual);
        btnBack   = findViewById(R.id.btnBack);
        viewPager = findViewById(R.id.viewPager);

        // Set up adapter with 2 fragments
        PredictionPagerAdapter adapter = new PredictionPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // If launched from MainActivity with a mode extra, jump to the right page
        String mode = getIntent().getStringExtra("mode");
        if ("manual".equals(mode)) {
            viewPager.setCurrentItem(1, false);
        }

        // Update header + tab highlight when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tvHeader.setText("חיזוי לפי משחק");
                    tabGame.setTextColor(0xFFC084FC);
                    tabGame.setBackgroundResource(R.drawable.tab_selected_bg);
                    tabManual.setTextColor(0xFF4A3F5C);
                    tabManual.setBackgroundResource(0);
                } else {
                    tvHeader.setText("חיזוי ידני");
                    tabManual.setTextColor(0xFFC084FC);
                    tabManual.setBackgroundResource(R.drawable.tab_selected_bg);
                    tabGame.setTextColor(0xFF4A3F5C);
                    tabGame.setBackgroundResource(0);
                }
            }
        });

        // Tab click listeners
        tabGame.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        tabManual.setOnClickListener(v -> viewPager.setCurrentItem(1, true));

        btnBack.setOnClickListener(v -> finish());
    }
}