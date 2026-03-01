package com.example.mainapp.Screens.Predictions;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class GamePredictionFragment extends Fragment {

    private ArrayList<Game> allGames;
    private Spinner spinnerGames;
    private TextView redAlliance, blueAlliance, predictionTxt;
    private Button predictBtn;
    private int currentGameID = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_prediction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerGames  = view.findViewById(R.id.spinnerGames);
        redAlliance   = view.findViewById(R.id.txtRedAlliance);
        blueAlliance  = view.findViewById(R.id.txtBlueAlliance);
        predictionTxt = view.findViewById(R.id.txtPredictionResult);
        predictBtn    = view.findViewById(R.id.btnPredict);

        initGames();
        onClickGame();

        predictBtn.setOnClickListener(v -> {
            if (currentGameID == 0) {
                predictionTxt.setText("בחר משחק לקבלת חיזוי");
                return;
            }
            predictionTxt.setText("מחשב חיזוי...");
            predictionTxt.setTextColor(0xFF7C6F8E);
            Game game = allGames.get(currentGameID - 1);
            getAllTeamAverages(game.getRedAlliance(), game.getBlueAlliance(), (redAvg, blueAvg) ->
                    requireActivity().runOnUiThread(() -> {
                        String prediction;
                        if (redAvg > blueAvg) {
                            prediction = "הברית האדומה תנצח! 🔴\n";
                            predictionTxt.setTextColor(Color.rgb(255, 80, 80));
                        } else if (blueAvg > redAvg) {
                            prediction = "הברית הכחולה תנצח! 🔵\n";
                            predictionTxt.setTextColor(Color.rgb(80, 140, 255));
                        } else {
                            prediction = "תיקו! ⚖️\n";
                            predictionTxt.setTextColor(0xFFF0E6FF);
                        }
                        prediction += String.format("אדום: %.2f | כחול: %.2f", redAvg, blueAvg);
                        predictionTxt.setText(prediction);
                    })
            );
        });
    }

    private void initGames() {
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.GameCallback() {
            @Override
            public void onSuccess(ArrayList<Game> games) {
                allGames = games;
                requireActivity().runOnUiThread(() -> initSpinner());
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void initSpinner() {
        ArrayList<String> gameNames = new ArrayList<>();
        gameNames.add("בחר משחק");
        if (allGames != null) {
            for (Game game : allGames) gameNames.add(game.getGameTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.simple_spinner_item, gameNames);
        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinnerGames.setAdapter(adapter);
    }

    private void onClickGame() {
        spinnerGames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && allGames != null) {
                    currentGameID = position;
                    Game wantedGame = allGames.get(position - 1);
                    StringBuilder red = new StringBuilder();
                    StringBuilder blue = new StringBuilder();
                    for (int i = 0; i < 3; i++) {
                        red.append(wantedGame.getRedAlliance()[i].getTeamNumber()).append("  ");
                        blue.append(wantedGame.getBlueAlliance()[i].getTeamNumber()).append("  ");
                    }
                    redAlliance.setText(red.toString().trim());
                    redAlliance.setTextColor(Color.rgb(255, 80, 80));
                    blueAlliance.setText(blue.toString().trim());
                    blueAlliance.setTextColor(Color.rgb(80, 140, 255));
                    predictionTxt.setText("לחץ על חשב חיזוי");
                    predictionTxt.setTextColor(0xFF7C6F8E);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void getAllTeamAverages(Team[] redTeams, Team[] blueTeams, AllianceCallback callback) {
        CountDownLatch latch = new CountDownLatch(6);
        double[] avgs = new double[6];
        for (int i = 0; i < 3; i++) {
            final int ri = i, bi = i + 3;
            DataHelper.getInstance().getAvgOfTeam(redTeams[i].getTeamNumber(), 1, new DataHelper.DataCallback<Double>() {
                @Override public void onSuccess(Double d) { avgs[ri] = d; latch.countDown(); }
                @Override public void onFailure(String e) { avgs[ri] = 0; latch.countDown(); }
            });
            DataHelper.getInstance().getAvgOfTeam(blueTeams[i].getTeamNumber(), 1, new DataHelper.DataCallback<Double>() {
                @Override public void onSuccess(Double d) { avgs[bi] = d; latch.countDown(); }
                @Override public void onFailure(String e) { avgs[bi] = 0; latch.countDown(); }
            });
        }
        new Thread(() -> {
            try {
                latch.await();
                double red  = (avgs[0] + avgs[1] + avgs[2]) / 3.0;
                double blue = (avgs[3] + avgs[4] + avgs[5]) / 3.0;
                callback.onResult(red, blue);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }

    interface AllianceCallback {
        void onResult(double redAvg, double blueAvg);
    }
}