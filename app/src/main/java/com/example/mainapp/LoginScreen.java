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

import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.PasswordHasherUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;


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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (userName.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(userName, password);
            }
        });

        tvSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignupScreen.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String userName, String password){
        DataHelper.getInstance().readUserByUsername(userName, new DataHelper.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);


                if (user.getPassword().equals(PasswordHasherUtils.hashPassword(password))) {
                    // Login successful
                    Toast.makeText(context, "התחברת בהצלחה! ברוך הבא " + user.getFullName(), Toast.LENGTH_SHORT).show();
                    SharedPrefHelper.getInstance(context).saveUser(user.getUserID(), user.getUserName(), user.getFullName());
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);

                } else {
                    // Incorrect password
                    Toast.makeText(context, "סיסמה שגויה", Toast.LENGTH_SHORT).show();
                    etPassword.setError("סיסמה שגויה");
                    etPassword.requestFocus();
                }
            }

            @Override
            public void onFailure(String error) {

                // Show error message
                if (error.equals("User not found")) {
                    Toast.makeText(context, "שם משתמש לא נמצא", Toast.LENGTH_SHORT).show();
                    etUserId.setError("משתמש לא קיים");
                    etUserId.requestFocus();
                } else {
                    Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        context = LoginScreen.this;
    }
}
