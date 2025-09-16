package com.example.mainapp;

import static android.app.ProgressDialog.show;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;

public class SignupScreen extends AppCompatActivity {


    private EditText etFullName, etUserId, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginLink;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        init();
        // הגדרת לחיצה על כפתור הרשמה
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String userId = etUserId.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // בדיקות בסיסיות
                if (fullName.isEmpty() || userId.isEmpty() || email.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(context, "הסיסמא חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(context, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(context, "נרשמת בהצלחה! שם: " + fullName, Toast.LENGTH_LONG).show();

                // חזרה למסך התחברות
                finish();
            }
        });

        // הגדרת לחיצה על קישור התחברות
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה למסך התחברות
                finish();
            }
        });
    }
    private void init(){
        etFullName = findViewById(R.id.etFullName);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        context = SignupScreen.this;
    }
}