package com.example.mainapp.Screens.Predictions;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.Game;

import java.util.ArrayList;

public class GamePrediction extends AppCompatActivity {
    private ArrayList<Game> allGames;
    private Spinner spinnerGames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_prediction);
        init();

    }
    private void init(){
        this.spinnerGames = findViewById(R.id.spinnerGames);
        initGames();
    }
    private void initGames(){
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.GameCallback() {
            @Override
            public void onSuccess(ArrayList<Game> games) {
                allGames = games;

                runOnUiThread(()->initSpinner());
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
    private void initSpinner(){
        ArrayList<String> gameNames = new ArrayList<>();
        gameNames.add("בחר משחק");

        if(allGames != null) {
            for(Game game : allGames){
                String gameName = game.getGameTitle();
                gameNames.add(gameName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                GamePrediction.this,
                R.layout.simple_spinner_item,
                gameNames
        );
        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinnerGames.setAdapter(adapter);
    }
}