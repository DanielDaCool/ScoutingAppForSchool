package com.example.mainapp.Adapters;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Screens.TeamProfileActivity;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

/**
 * Represents the TeamStatsAdapter component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class TeamStatsAdapter extends RecyclerView.Adapter<TeamStatsAdapter.TeamStatsViewHolder> {

    private ArrayList<TeamStats> teamStats;
    private Context context;

    public TeamStatsAdapter(ArrayList<TeamStats> teamStats) {
        this.teamStats = teamStats;
    }

    @NonNull
    @Override
/**
 * Creates and inflates a new RecyclerView item layout.
 * @param parent parameter required for this method.
 * @param viewType parameter required for this method.
 * @return the value produced by this method.
 */
    public TeamStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_stats, parent, false);
        return new TeamStatsViewHolder(view);
    }

    @Override
/**
 * Binds data from the current item into the RecyclerView row.
 * @param holder parameter required for this method.
 * @param position parameter required for this method.
 */
    public void onBindViewHolder(@NonNull TeamStatsViewHolder holder, int position) {
        TeamStats curTeamStats = teamStats.get(position);


        int teamNumber = curTeamStats.getTeam().getTeamNumber();
        String teamName = curTeamStats.getTeam().getTeamName();

        double avgPoints = curTeamStats.calculateAvgPoints();
        double avgGamePieces = curTeamStats.getAvgGamePieceCount();
        GamePiece mostScoredPiece = curTeamStats.getMostScoredGamePiece();

        holder.tvTeamNumber.setText(String.valueOf(teamNumber));
        holder.tvTeamName.setText(teamName);
        holder.tvAvgPoints.setText("ממוצע נקודות: " + String.format("%.1f", Double.isNaN(avgPoints) ? 0 : avgPoints));
        holder.tvGamePieces.setText("ממוצע חלקי משחק: " + String.format("%.1f", Double.isNaN(avgGamePieces) ? 0 : avgGamePieces));

        String mostScoredText = mostScoredPiece != null
                ? "הגובה הממוצע: " + mostScoredPiece.name()
                : "הגובה הממוצע: אין מידע";
        holder.tvMostScored.setText(mostScoredText);

        holder.tvGamesPlayed.setText("כמות משחקים: " + curTeamStats.getGamesPlayed());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TeamProfileActivity.class);
            intent.putExtra(TeamProfileActivity.EXTRA_TEAM_NUMBER, curTeamStats.getTeam().getTeamNumber());
            context.startActivity(intent);
        });
    }



    @Override
/**
 * Returns the number of items displayed by the adapter.
 * @return the value produced by this method.
 */
    public int getItemCount() {
        return teamStats.size();
    }

/**
 * Executes the logic associated with the updateData operation.
 * @param newData parameter required for this method.
 */
    public void updateData(ArrayList<TeamStats> newData) {
        this.teamStats = newData;
        notifyDataSetChanged();
    }


    public static class TeamStatsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamNumber;
        TextView tvTeamName;
        TextView tvAvgPoints;
        TextView tvGamePieces;
        TextView tvMostScored;
        TextView tvGamesPlayed;

        public TeamStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamNumber = itemView.findViewById(R.id.teamNumberTextView);
            tvTeamName = itemView.findViewById(R.id.teamNameTextView);
            tvAvgPoints = itemView.findViewById(R.id.avgPointsTextView);
            tvGamePieces = itemView.findViewById(R.id.avgGamePiecesTextView);
            tvMostScored = itemView.findViewById(R.id.mostScoredTextView);
            tvGamesPlayed = itemView.findViewById(R.id.gamesPlayedTextView);
        }
    }
}