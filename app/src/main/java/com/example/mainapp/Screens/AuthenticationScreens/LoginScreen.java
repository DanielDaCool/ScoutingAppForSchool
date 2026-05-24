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
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;

/**
 * Represents the LoginScreen component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class LoginScreen extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin;
    private TextView tvSignupLink;
    private Context  context;

    @Override
/**
 * Initializes the activity and prepares the screen components and data.
 * @param savedInstanceState parameter required for this method.
 */
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
            if (!InternetUtils.isInternetConnected(context)) {
                Toast.makeText(context, "אין חיבור לאינטרנט", Toast.LENGTH_LONG).show();
                return;
            }
            btnLogin.setEnabled(false);
            loginUser(email, password);
        });

        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(context, SignupScreen.class))
        );
    }

/**
 * Executes the logic associated with the loginUser operation.
 * @param email parameter required for this method.
 * @param password parameter required for this method.
 */
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

                    showDistrictPickerDialog(user.isAdmin());

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


/**
 * Executes the logic associated with the showDistrictPickerDialog operation.
 * @param isAdmin parameter required for this method.
 */
    private void showDistrictPickerDialog(boolean isAdmin) {
        EVENTS[] events     = EVENTS.values();
        String[] eventNames = new String[events.length];
        for (int i = 0; i < events.length; i++) eventNames[i] = events[i].toString();

        new AlertDialog.Builder(context)
                .setTitle(isAdmin ? "בחר תחרות לצפייה בנתונים" : "בחר תחרות לסקאוטינג")
                .setCancelable(false) // must pick
                .setItems(eventNames, (dialog, which) -> {
                    SharedPrefHelper.getInstance(context).saveDistrict(events[which]);
                    navigateToLoading();
                })
                .show();
    }

/**
 * Executes the logic associated with the navigateToLoading operation.
 */
    private void navigateToLoading() {
        startActivity(new Intent(context, LoadingScreen.class));
        finish();
    }

/**
 * Executes the logic associated with the init operation.
 */
    private void init() {
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        context      = LoginScreen.this;
    }
}