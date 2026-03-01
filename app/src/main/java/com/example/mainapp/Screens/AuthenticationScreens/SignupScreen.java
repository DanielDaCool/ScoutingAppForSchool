package com.example.mainapp.Screens.AuthenticationScreens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;

public class SignupScreen extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginLink;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        init();

        btnSignup.setOnClickListener(v -> {
            String fullName       = etFullName.getText().toString().trim();
            String email          = etEmail.getText().toString().trim();
            String password       = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("אימייל לא תקין");
                etEmail.requestFocus();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(context, "הסיסמא חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(context, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
                etConfirmPassword.setError("הסיסמאות אינן תואמות");
                return;
            }

            btnSignup.setEnabled(false);

            DataHelper.getInstance().registerUser(fullName, email, password, new DataHelper.DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        Toast.makeText(context,
                                "נרשמת בהצלחה! ברוך הבא " + user.getFullName(),
                                Toast.LENGTH_LONG).show();
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        btnSignup.setEnabled(true);
                        if (error != null && error.contains("email address is already in use")) {
                            Toast.makeText(context, "אימייל זה כבר רשום במערכת", Toast.LENGTH_LONG).show();
                            etEmail.setError("אימייל כבר קיים");
                            etEmail.requestFocus();
                        } else {
                            Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void init() {
        etFullName       = findViewById(R.id.etFullName);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup        = findViewById(R.id.btnSignup);
        tvLoginLink      = findViewById(R.id.tvLoginLink);
        context          = SignupScreen.this;
    }
}