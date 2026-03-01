package com.example.mainapp.Screens.AuthenticationScreens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.MainActivity;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;
import com.example.mainapp.Utils.SharedPrefHelper;

public class LoginScreen extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        init();

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            loginUser(email, password);
        });

        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(context, SignupScreen.class))
        );
    }

    private void loginUser(String email, String password) {
        DataHelper.getInstance().loginUser(email, password, new DataHelper.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(context,
                            "התחברת בהצלחה! ברוך הבא " + user.getFullName(),
                            Toast.LENGTH_SHORT).show();
                    SharedPrefHelper.getInstance(context)
                            .saveUser(user.getFullName(), user.getEmail());
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if ("User not found".equals(error)) {
                        Toast.makeText(context, "אימייל לא נמצא", Toast.LENGTH_SHORT).show();
                        etEmail.setError("משתמש לא קיים");
                        etEmail.requestFocus();
                    } else if ("Wrong password".equals(error)) {
                        Toast.makeText(context, "סיסמה שגויה", Toast.LENGTH_SHORT).show();
                        etPassword.setError("סיסמה שגויה");
                        etPassword.requestFocus();
                    } else {
                        Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void init() {
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        context    = LoginScreen.this;
    }
}