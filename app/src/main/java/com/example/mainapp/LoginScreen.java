package com.example.mainapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginScreen extends AppCompatActivity {
    private EditText etUserId, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        init();

        // הגדרת לחיצה על כפתור התחברות
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = etUserId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // בדיקה בסיסית
                if (userId.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                // הודעה זמנית - כאן תוסיף את לוגיקת ההתחברות
                Toast.makeText(context, "מתחבר עם ID: " + userId, Toast.LENGTH_SHORT).show();
            }
        });

        // הגדרת לחיצה על קישור הרשמה
        tvSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר למסך הרשמה
                Intent intent = new Intent(context, SignupScreen.class);
                startActivity(intent);
            }
        });
    }

    private void init() {

        // חיבור הרכיבים
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        context = LoginScreen.this;
    }
}
