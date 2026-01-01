package com.example.mainapp.Screens.Predictions;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;

import java.util.ArrayList;

public class GamePrediction extends AppCompatActivity {
    private ArrayList<Game> allGames;
    private Spinner spinnerGames;
    private TextView redAlliance;
    private TextView blueAlliance;
    private TextView predictionTxt;
    private Button predictBtn;
    private Team[] redAllianceTeams;
    private Team[] blueAllianceTeams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_prediction);
        init();

    }
    private void init(){
        this.spinnerGames = findViewById(R.id.spinnerGames);
        this.redAlliance = findViewById(R.id.txtRedAlliance);
        this.blueAlliance = findViewById(R.id.txtBlueAlliance);
        this.predictionTxt = findViewById(R.id.txtPredictionResult);
        this.predictBtn = findViewById(R.id.btnPredict);
        this.redAllianceTeams = new Team[3];
        this.blueAllianceTeams = new Team[3];
        initGames();

        onClickGame();
        onClickButton();
    }



    private void onClickButton(){
        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                }

        });

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

    private void onClickGame(){
        spinnerGames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) {


                    runOnUiThread(() -> {

                        Game wantedGame = allGames.get(position - 1);
                        blueAllianceTeams = wantedGame.getBlueAlliance();
                        redAllianceTeams = wantedGame.getRedAlliance();
                        String redAllianceString = "";
                        String blueAllianceString = "";
                        for (int i = 0; i < 3; i++) {
                            redAllianceString += wantedGame.getRedAlliance()[i].getTeamNumber() + " ";
                            blueAllianceString += wantedGame.getBlueAlliance()[i].getTeamNumber() + " ";
                        }
                        redAlliance.setText(redAllianceString);
                        redAlliance.setTextColor(Color.rgb(255, 0, 0));
                        blueAlliance.setText(blueAllianceString);

                        blueAlliance.setTextColor(Color.rgb(0, 0, 255));
                        predictionTxt.setText("לחץ על חשב חיזוי על מנת לקבל חיזוי על הברית המנצחת");


                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}