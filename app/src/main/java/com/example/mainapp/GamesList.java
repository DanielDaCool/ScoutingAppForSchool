package com.example.mainapp;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.GameAdapter;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils;

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

        // Setup RecyclerView with adapter
        setupRecyclerView();
        listFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String input = listFilter.getText().toString().trim();

                    if (input.isEmpty()) {
                        filteredGameList = new ArrayList<>(gameList);
                        updateToFiltered();
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
    private void copyData(ArrayList<Game> from, ArrayList<Game> to){

    }
    private void showFilteredGames(int teamNumber){
        filteredGameList = new ArrayList<>(gameList);
        filteredGameList.removeIf(game -> !TeamUtils.ContainsTeam(game.getPlayingTeamsNumbers(), teamNumber));
        updateToFiltered();


    }
    private void updateToFiltered(){
        gameAdapter = new GameAdapter(filteredGameList);
        setupRecyclerView();


    }
    private void init(){
        recyclerView = findViewById(R.id.gamesList);
        gameList = DataHelper.getGames(Constants.currentEventOnApp);
        filteredGameList = gameList;
        gameAdapter = new GameAdapter(gameList);
        context = GamesList.this;
        listFilter = findViewById(R.id.editTextTeamFilter);
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.gamesList);
    }

    private void setupRecyclerView() {

        gameAdapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game, int position) {
                Toast.makeText(context,
                        "בחרת: " + game.getGameTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Set layout manager (vertical list)
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set adapter to RecyclerView
        recyclerView.setAdapter(gameAdapter);
    }

    // Helper methods for dynamic operations
    public void addNewGame(Game newGame) {
        gameList.add(newGame);
        gameAdapter.notifyItemInserted(gameList.size() - 1);
    }

    public void removeGame(int position) {
        if (position >= 0 && position < gameList.size()) {
            gameList.remove(position);
            gameAdapter.notifyItemRemoved(position);
        }
    }
}