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

    private ArrayList<ArrayList<TeamAtGame>> allTeamsGames;

    // The constructor now takes the already aggregated list
    public TeamStatsAdapter(ArrayList<ArrayList<TeamAtGame>> allTeamsGamesAggregated) {
        this.allTeamsGames = allTeamsGamesAggregated;
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
        ArrayList<TeamAtGame> teamGames = allTeamsGames.get(position);

        if (teamGames.isEmpty()) {
            return;
        }

        int teamNumber = teamGames.get(0).getTeam().teamNumber();
        String teamName = teamGames.get(0).getTeam().teamName();

        double avgPoints = DataHelper.getAvgPoints(teamNumber);
        double avgGamePieces = TeamUtils.getAvgGamePiecePerGame(teamGames);
        GamePiece mostScoredPiece = TeamUtils.getMostScoredGamePiece(teamGames);

        holder.teamNumberTextView.setText(String.valueOf(teamNumber));
        holder.teamNameTextView.setText(teamName);
        holder.avgPointsTextView.setText("ממוצע נקודות: " + String.format("%.1f", avgPoints));
        holder.avgGamePiecesTextView.setText("ממוצע חלקי משחק: " + String.format("%.1f", avgGamePieces));

        String mostScoredText = mostScoredPiece != null
                ? "הגובה הממוצע: " + mostScoredPiece.name()
                : "הגובה הממוצע: אין מידע";
        holder.mostScoredTextView.setText(mostScoredText);

        holder.gamesPlayedTextView.setText("כמות משחקים: " + teamGames.size());
    }



    @Override
    public int getItemCount() {
        // Item count is the number of unique teams (size of the outer list)
        return allTeamsGames.size();
    }

    public void updateData(ArrayList<ArrayList<TeamAtGame>> newData) {
        this.allTeamsGames = newData;
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