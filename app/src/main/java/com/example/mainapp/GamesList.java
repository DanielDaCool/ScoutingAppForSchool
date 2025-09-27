package com.example.mainapp;

import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;

public class GamesList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameAdapter gameAdapter;
    private ArrayList<Game> gameList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);

        init();

        // Setup RecyclerView with adapter
        setupRecyclerView();
    }
    private void init(){
        recyclerView = findViewById(R.id.gamesList);
        gameList = DataHelper.getGames();
        gameAdapter = new GameAdapter(gameList);
        context = GamesList.this;
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