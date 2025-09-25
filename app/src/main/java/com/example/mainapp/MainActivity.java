package com.example.mainapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


        onClick(signupBtn, new Intent(MainActivity.this, SignupScreen.class));
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginScreen.class));
            }
        });

    }


    private void init() {
        this.loginBtn = findViewById(R.id.loginBtn);
        this.signupBtn = findViewById(R.id.signupBtn);


    }

    private void onClick(Button btn, Intent intent) {

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
    }
}