package com.example.mainapp;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import com.example.mainapp.Utils.SharedPrefHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button gamesListButton;
    private Button formsButton;
    private Button loginButton;
    private Button signupButton;
    private Button statsButton;
    private TextView welcomeText;
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
        setOnClickListener(formsButton, FormsActivity.class);
        setOnClickListener(signupButton, SignupScreen.class);
        setOnClickListener(loginButton, LoginScreen.class);
        setOnClickListener(statsButton, TeamStatsActivity.class);
        String userName = SharedPrefHelper.getInstance(context).getUserName();
        welcomeText.setText("שלום, " + userName);

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

        context = MainActivity.this;
        gamesListButton = findViewById(R.id.buttonGamesList);
        formsButton = findViewById(R.id.buttonForms);
        loginButton = findViewById(R.id.buttonLogin);
        signupButton = findViewById(R.id.buttonSignup);
        statsButton = findViewById(R.id.buttonStats);
        welcomeText = findViewById(R.id.textViewWelcome);


        if(!SharedPrefHelper.getInstance(context).isUserLoggedIn()){
            formsButton.setVisibility(GONE);
            statsButton.setVisibility(GONE);
            gamesListButton.setVisibility(GONE);

        }
        else{
            loginButton.setVisibility(GONE);
            signupButton.setVisibility(GONE);
            formsButton.setVisibility(VISIBLE);
            statsButton.setVisibility(VISIBLE);
            gamesListButton.setVisibility(VISIBLE);
        }
    }
}
