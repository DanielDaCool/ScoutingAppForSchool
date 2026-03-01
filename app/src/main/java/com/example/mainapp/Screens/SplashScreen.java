package com.example.mainapp.Screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.Adapters.EventDropdown;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.InternetReciver;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {

    private EventDropdown eventDropdown;
    private InternetReciver internetReciver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        internetReciver = new InternetReciver();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app")
                .setPersistenceEnabled(true);

        View dropdownView = findViewById(R.id.eventDropdown);
        eventDropdown = new EventDropdown(this, dropdownView);
        initTeams();

        eventDropdown.setOnEventSelectedListener(event -> {
            Constants.CURRENT_EVENT_ON_APP = event;

            // Go to loading screen after short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(SplashScreen.this, LoadingScreen.class));
                finish();
            }, 500);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetReciver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(internetReciver);
        } catch (IllegalArgumentException ignored) {}
    }

    private void initTeams() {
        TBAApiManager.getInstance().getIsraeliTeams(new TBAApiManager.TeamListCallback() {
            @Override
            public void onSuccess(ArrayList<Team> teams) {
                for (Team t : teams) {
                    DataHelper.getInstance().isTeamDataExists(t, exists -> {
                        if (!exists) {
                            DataHelper.getInstance().createTeamStats(new TeamStats(t), null);
                        }
                    });
                }
            }
        });
    }
}