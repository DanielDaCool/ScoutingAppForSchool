package com.example.mainapp;
import com.example.mainapp.Utils.Team;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button gamesListButton;
    private Button formsButton;
    private Button loginButton;
    private Button signupButton;
    private Button statsButton;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app")
                .setPersistenceEnabled(true); // Optional: enable offline persistence

        init();
        setOnClickListener(gamesListButton,GamesList.class);
        setOnClickListener(formsButton, Forms.class);
        setOnClickListener(signupButton, SignupScreen.class);
        setOnClickListener(loginButton, LoginScreen.class);
        setOnClickListener(statsButton, TeamStatsActivity.class);
    }

    private void setOnClickListener(Button btn, Class classToGo){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, classToGo));
            }
        });
    }

    private void init() {

        gamesListButton = findViewById(R.id.buttonGamesList);
        formsButton = findViewById(R.id.buttonForms);
        loginButton = findViewById(R.id.buttonLogin);
        signupButton = findViewById(R.id.buttonSignup);
        statsButton = findViewById(R.id.buttonStats);
        context = MainActivity.this;
    }
}
