package com.example.mainapp.Screens;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.GameAdapter;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

import java.util.ArrayList;

public class GamesList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameAdapter gameAdapter;
    private EditText listFilter;
    private ArrayList<Game> gameList;
    private ArrayList<Game> filteredGameList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        init();

        // FIXED: Setup RecyclerView BEFORE loading data
        setupRecyclerView();

        // Load games from API
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.GameCallback() {
            @Override
            public void onSuccess(ArrayList<Game> games) {
                runOnUiThread(() -> {

                    

                    gameList.clear();
                    gameList.addAll(games);
                    filteredGameList.clear();
                    filteredGameList.addAll(games);

                    gameAdapter.notifyDataSetChanged();


                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    System.err.println("Error loading games: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(context, "Error loading games: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });

        listFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String input = listFilter.getText().toString().trim();

                    if (input.isEmpty()) {
                        filteredGameList.clear();
                        filteredGameList.addAll(gameList);
                        gameAdapter.notifyDataSetChanged();
                        return true;
                    }

                    try {
                        int teamNumber = Integer.parseInt(input);
                        if (teamNumber < 0 || teamNumber > 10000) {
                            Toast.makeText(context, "מספר קבוצה לא חוקי", Toast.LENGTH_SHORT).show();
                            listFilter.setText("");
                        } else {
                            showFilteredGames(teamNumber);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "תכניס מספר אמיתי", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void showFilteredGames(int teamNumber){
        filteredGameList.clear();
        for (Game game : gameList) {
            if (TeamUtils.ContainsTeam(game.getPlayingTeamsNumbers(), teamNumber)) {
                filteredGameList.add(game);
            }
        }
        gameAdapter.notifyDataSetChanged();
    }

    private void init(){
        recyclerView = findViewById(R.id.gamesList);
        context = GamesList.this;
        listFilter = findViewById(R.id.editTextTeamFilter);

        // FIXED: Create separate ArrayList instances
        gameList = new ArrayList<>();
        filteredGameList = new ArrayList<>();

        gameAdapter = new GameAdapter(filteredGameList);
    }

    private void setupRecyclerView() {
        gameAdapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game, int position) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(gameAdapter);
    }

    public void addNewGame(Game newGame) {
        gameList.add(newGame);
        filteredGameList.add(newGame);
        gameAdapter.notifyItemInserted(filteredGameList.size() - 1);
    }

    public void removeGame(int position) {
        if (position >= 0 && position < filteredGameList.size()) {
            Game gameToRemove = filteredGameList.get(position);
            gameList.remove(gameToRemove);
            filteredGameList.remove(position);
            gameAdapter.notifyItemRemoved(position);
        }
    }
}