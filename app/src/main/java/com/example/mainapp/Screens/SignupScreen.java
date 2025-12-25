package com.example.mainapp.Screens;
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
import com.example.mainapp.Utils.DatabaseUtils.PasswordHasherUtils;
import com.example.mainapp.Utils.DatabaseUtils.User;

public class    SignupScreen extends AppCompatActivity {


    private EditText etFullName, etUserId, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginLink;
    private Context context;
    private int userID;


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
                String userName = etUserId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // בדיקות בסיסיות
                if (fullName.isEmpty() || userName.isEmpty() ||
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

                // Show loading state
                btnSignup.setEnabled(false);

                String hashedPass = PasswordHasherUtils.hashPassword(password);

                // Use userId from the EditText, not a local counter
                User newUser = new User(fullName, -1, hashedPass, userName);

                DataHelper.getInstance().createUser(newUser, new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        System.out.println("SUCCESS: User created with ID: " + id);
                        Toast.makeText(context, "נרשמת בהצלחה! שם: " + fullName, Toast.LENGTH_LONG).show();

                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        System.out.println("FAILURE: " + error);
                        Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_LONG).show();

                        // Re-enable button on failure
                        btnSignup.setEnabled(true);
                    }
                });

                // ✅ REMOVED finish() from here!
            }
        });

        // הגדרת לחיצה על קישור התחברות
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }
    private void init(){
        userID = 0;
        etFullName = findViewById(R.id.etFullName);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        context = SignupScreen.this;
    }

}