package com.example.mainapp.Adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.TeamAtGame;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.DataHelper;
import com.example.mainapp.Utils.TeamUtils;

import java.util.ArrayList;
import java.util.Locale;

public class TeamStatsAdapter extends RecyclerView.Adapter<TeamStatsAdapter.TeamStatsViewHolder> {

    // The data source is now the list of ALL teams, where each element is a list of that team's games
    private ArrayList<ArrayList<TeamAtGame>> allTeamsGamesAggregated;

    // The constructor now takes the already aggregated list
    public TeamStatsAdapter(ArrayList<ArrayList<TeamAtGame>> allTeamsGamesAggregated) {
        this.allTeamsGamesAggregated = allTeamsGamesAggregated;
    }

    // Removed the getUniqueTeamNumbers and the aggregation method as it's now in the Activity

    @NonNull
    @Override
    public TeamStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_stats, parent, false);
        return new TeamStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamStatsViewHolder holder, int position) {
        // The item at 'position' is the complete history for one team
        ArrayList<TeamAtGame> teamGames = allTeamsGamesAggregated.get(position);

        if (teamGames.isEmpty()) {
            return;
        }

        // Use the first game object to get the team's constant info (number, name)
        TeamAtGame representativeGame = teamGames.get(0);
        int teamNumber = representativeGame.getTeam().teamNumber();
        String teamName = representativeGame.getTeam().teamName();

        // Calculate aggregate statistics for this team
        double avgPoints = DataHelper.getAvgPoints(teamNumber);
        double avgGamePieces = TeamUtils.getAvgGamePiecePerGame(teamGames);
        GamePiece mostScoredPiece = TeamUtils.getMostScoredGamePiece(teamGames);

        // Set the views
        holder.teamNumberTextView.setText(String.valueOf(teamNumber));
        holder.teamNameTextView.setText(teamName);
        holder.avgPointsTextView.setText(String.format(Locale.getDefault(), "Avg: %.1f pts", avgPoints));
        holder.avgGamePiecesTextView.setText(String.format(Locale.getDefault(), "Avg pieces: %.1f", avgGamePieces));

        String mostScoredText = mostScoredPiece != null
                ? "Most scored: " + mostScoredPiece.name()
                : "Most scored: None";
        holder.mostScoredTextView.setText(mostScoredText);

        // Display the total number of games played by this team
        holder.gamesPlayedTextView.setText(String.format(Locale.getDefault(), "Games: %d", teamGames.size()));
    }



    @Override
    public int getItemCount() {
        // Item count is the number of unique teams (size of the outer list)
        return allTeamsGamesAggregated.size();
    }

    public void updateData(ArrayList<ArrayList<TeamAtGame>> newAggregatedData) {
        this.allTeamsGamesAggregated = newAggregatedData;
        notifyDataSetChanged();
    }

    public static class TeamStatsViewHolder extends RecyclerView.ViewHolder {
        TextView teamNumberTextView;
        TextView teamNameTextView;
        TextView avgPointsTextView;
        TextView avgGamePiecesTextView;
        TextView mostScoredTextView;
        TextView gamesPlayedTextView;

        public TeamStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            teamNumberTextView = itemView.findViewById(R.id.teamNumberTextView);
            teamNameTextView = itemView.findViewById(R.id.teamNameTextView);
            avgPointsTextView = itemView.findViewById(R.id.avgPointsTextView);
            avgGamePiecesTextView = itemView.findViewById(R.id.avgGamePiecesTextView);
            mostScoredTextView = itemView.findViewById(R.id.mostScoredTextView);
            gamesPlayedTextView = itemView.findViewById(R.id.gamesPlayedTextView);
        }
    }
}