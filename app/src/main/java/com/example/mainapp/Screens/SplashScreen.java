package com.example.mainapp.Screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.Adapters.EventDropdown;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {

    private EventDropdown eventDropdown;
    private TextView selectedEventText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app")
                .setPersistenceEnabled(true);

        // Initialize dropdown
        View dropdownView = findViewById(R.id.eventDropdown);
        eventDropdown = new EventDropdown(this, dropdownView);
        initTeams();

        // Set listener for event selection
        eventDropdown.setOnEventSelectedListener(event -> {
            Constants.CURRENT_EVENT_ON_APP = event;
            Log.d("EVENT: ", event.toString());
            Handler handler = new Handler(Looper.getMainLooper());

            handler.postDelayed(() -> {
                goToMain();

            }, 1000);
        });


    }




    private void initTeams() {

        ArrayList<TeamStats> temp = new ArrayList<TeamStats>();
        TBAApiManager.getInstance().getIsraeliTeams(new TBAApiManager.TeamListCallback() {
            @Override
            public void onSuccess(ArrayList<Team> teams) {
                for(Team t : teams){
                    DataHelper.getInstance().isTeamDataExists(t, new DataHelper.ExistsCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if(!exists){
                                DataHelper.getInstance().createTeamStats(new TeamStats(t), null);
                                temp.add(new TeamStats(t));
                                Log.d("SplashScreen", "Added new team: " + t.getTeamNumber());

                            }
                        }
                    });
                }
                for(TeamStats teamStats : temp){
                    DataHelper.getInstance().createTeamStats(teamStats, null);
                }
            }
        });

    }
    private void goToMain(){
        boolean isUserLogged = SharedPrefHelper.getInstance(SplashScreen.this).isUserLoggedIn();
        Intent i;
        if(isUserLogged) i = new Intent(SplashScreen.this, MainActivity.class);
        else i = new Intent(SplashScreen.this, LoginScreen.class);
        startActivity(i);

    }


}