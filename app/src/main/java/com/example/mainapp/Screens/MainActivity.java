package com.example.mainapp.Screens;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import com.example.mainapp.R;
import com.example.mainapp.Screens.Predictions.GamePrediction;
import com.example.mainapp.Screens.Predictions.PredictionScreen;
import com.example.mainapp.Utils.SharedPrefHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private Button logoutButton;

    private Button predictButton;
    private TextView welcomeText;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setOnClickListener(gamesListButton,GamesList.class);
        setOnClickListener(formsButton, FormsActivity.class);
        setOnClickListener(signupButton, SignupScreen.class);
        setOnClickListener(loginButton, LoginScreen.class);
        setOnClickListener(statsButton, TeamStatsActivity.class);
        setOnClickListener(predictButton, PredictionScreen.class);
        String userName = SharedPrefHelper.getInstance(context).getUserName();

        welcomeText.setText("שלום, " + userName);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setMessage("אתה בטוח שאתה רוצה להתנתק?");
                b.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPrefHelper.getInstance(context).logout();
                        activateLogout();
                    }
                });
                b.setNegativeButton("לא, חזור למסך הראשי", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = b.create();
                dialog.show();
            }
        });
    }

    private void setOnClickListener(Button btn, Class classToGo){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, classToGo));
            }
        });
    }



    private void activateLogout(){
        formsButton.setVisibility(GONE);
        statsButton.setVisibility(GONE);
        gamesListButton.setVisibility(GONE);
        logoutButton.setVisibility(GONE);
        predictButton.setVisibility(GONE);
        loginButton.setVisibility(VISIBLE);
        signupButton.setVisibility(VISIBLE);
        welcomeText.setText("שלום, משתמש" );
    }
    private void init() {

        context = MainActivity.this;
        gamesListButton = findViewById(R.id.buttonGamesList);
        formsButton = findViewById(R.id.buttonForms);
        loginButton = findViewById(R.id.buttonLogin);
        signupButton = findViewById(R.id.buttonSignup);
        statsButton = findViewById(R.id.buttonStats);
        welcomeText = findViewById(R.id.textViewWelcome);
        logoutButton = findViewById(R.id.buttonLogout);
        predictButton = findViewById(R.id.predictButton);



        if(!SharedPrefHelper.getInstance(context).isUserLoggedIn()){
            formsButton.setVisibility(GONE);
            statsButton.setVisibility(GONE);
            gamesListButton.setVisibility(GONE);
            logoutButton.setVisibility(GONE);
            predictButton.setVisibility(GONE);

        }
        else{
            loginButton.setVisibility(GONE);
            signupButton.setVisibility(GONE);
            formsButton.setVisibility(VISIBLE);
            statsButton.setVisibility(VISIBLE);
            gamesListButton.setVisibility(VISIBLE);
            logoutButton.setVisibility(VISIBLE);
            predictButton.setVisibility(VISIBLE);
        }
    }
}
