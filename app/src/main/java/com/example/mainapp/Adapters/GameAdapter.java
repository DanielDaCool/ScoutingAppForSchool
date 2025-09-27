package com.example.mainapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.Game;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    private List<Game> games;
    private OnItemClickListener listener;

    // Interface for handling click events
    public interface OnItemClickListener {
        void onItemClick(Game game, int position);
    }

    // Constructor
    public GameAdapter(List<Game> games) {
        this.games = games;
    }

    // Method to set click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_layout, parent, false);
        return new ViewHolder(view);
    }

    // REQUIRED METHOD 2: Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game, position);
    }

    // REQUIRED METHOD 3: Return total number of items
    @Override
    public int getItemCount() {
        return games.size();
    }

    // ViewHolder inner class
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameTitle;
        private TextView gameDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            gameTitle = itemView.findViewById(R.id.item_title);
            gameDescription = itemView.findViewById(R.id.item_description);
        }

        public void bind(Game game, int position) {
            // Set data to views
            gameTitle.setText(game.getGameTitle());
            gameDescription.setText(game.getDescription());

            // Handle click events
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(game, position);
                }
            });
        }
    }
}
