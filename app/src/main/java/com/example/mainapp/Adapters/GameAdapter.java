package com.example.mainapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.Game;

import java.util.List;

/**
 * GameAdapter is a RecyclerView adapter used to display a list of games (matches).
 * It shows the game title and a description, typically listing the competing teams.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games;
    private OnItemClickListener listener;

    /**
     * Interface definition for a callback to be invoked when a game item is clicked.
     */
    public interface OnItemClickListener {
        /**
         * Called when a game has been clicked.
         * @param game The game object that was clicked.
         * @param position The position of the item in the adapter.
         */
        void onItemClick(Game game, int position);
    }

    public GameAdapter(List<Game> games) {
        this.games = games;
    }

    /**
     * Sets the click listener for game items.
     * @param listener The listener to be notified of click events.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
/**
 * Creates and inflates a new RecyclerView item layout.
 * @param parent parameter required for this method.
 * @param viewType parameter required for this method.
 * @return the value produced by this method.
 */
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_layout, parent, false);
        return new GameViewHolder(view);
    }

    @Override
/**
 * Binds data from the current item into the RecyclerView row.
 * @param holder parameter required for this method.
 * @param position parameter required for this method.
 */
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game, position);
    }

    @Override
/**
 * Returns the number of items displayed by the adapter.
 * @return the value produced by this method.
 */
    public int getItemCount() {
        return games.size();
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {
        private TextView gameTitle;
        private TextView gameDescription;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.item_title);
            gameDescription = itemView.findViewById(R.id.item_description);
        }

        /**
         * Binds a game object to the view holder's UI elements.
         * @param game The game to bind.
         * @param position The position of the item in the list.
         */
        public void bind(Game game, int position) {
            gameTitle.setText(game.getGameTitle());
            gameDescription.setText(game.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(game, position);
                }
            });
        }
    }
}