package com.example.mainapp;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the buttons by their ID
        Button gamesListButton = findViewById(R.id.buttonGamesList);
        Button formsButton = findViewById(R.id.buttonForms);

        // Set up the click listener for the Games List button
//        gamesListButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Create an Intent to start the GamesListActivity
//                Intent intent = new Intent(MainActivity.this, GamesListActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        // Set up the click listener for the Forms button
//        formsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Create an Intent to start the FormsActivity
//                Intent intent = new Intent(MainActivity.this, FormsActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
