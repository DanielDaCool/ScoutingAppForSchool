package com.example.mainapp.Screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must be called before ANY Firebase usage
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance(
                "https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app"
        ).setPersistenceEnabled(true);

        SharedPrefHelper prefs = SharedPrefHelper.getInstance(this);

        if (prefs.isUserLoggedIn()) {
            startActivity(new Intent(this, LoadingScreen.class));
        } else {
            startActivity(new Intent(this, LoginScreen.class));
        }
        finish();
    }
}