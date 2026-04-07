package com.example.mainapp.Screens.AuthenticationScreens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.LoadingScreen;
import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;
import com.example.mainapp.Utils.SharedPrefHelper;

public class LoginScreen extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin;
    private TextView tvSignupLink;
    private Context  context;

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
            @Override public void onSuccess(User user) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    SharedPrefHelper.getInstance(context).saveUser(
                            user.getFullName(), user.getEmail(),
                            user.getUserId(), user.getRole());
                    Toast.makeText(context,
                            "ברוך הבא " + user.getFullName(), Toast.LENGTH_SHORT).show();

                    if (user.isAdmin()) {
                        navigateToLoading(); // admin skips district picker
                    } else {
                        showDistrictPickerDialog(); // scouter must pick district
                    }
                });
            }
            @Override public void onFailure(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if ("User not found".equals(error)) {
                        etEmail.setError("משתמש לא קיים");
                        etEmail.requestFocus();
                    } else if ("Wrong password".equals(error)) {
                        etPassword.setError("סיסמה שגויה");
                        etPassword.requestFocus();
                    } else {
                        Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showDistrictPickerDialog() {
        EVENTS[] events     = EVENTS.values();
        String[] eventNames = new String[events.length];
        for (int i = 0; i < events.length; i++) eventNames[i] = events[i].toString();

        new AlertDialog.Builder(context)
                .setTitle("בחר מחוז לסקאוטינג")
                .setCancelable(false) // must pick
                .setItems(eventNames, (dialog, which) -> {
                    SharedPrefHelper.getInstance(context).saveDistrict(events[which]);
                    navigateToLoading();
                })
                .show();
    }

    private void navigateToLoading() {
        startActivity(new Intent(context, LoadingScreen.class));
        finish();
    }

    private void init() {
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        context      = LoginScreen.this;
    }
}