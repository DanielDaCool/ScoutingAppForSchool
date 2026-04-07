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

public class TeamStatsAdapter extends RecyclerView.Adapter<TeamStatsAdapter.TeamStatsViewHolder> {

    private ArrayList<TeamStats> teamStats;
    private Context context;

    public TeamStatsAdapter(ArrayList<TeamStats> teamStats) {
        this.teamStats = teamStats;
    }

    @NonNull
    @Override
    public TeamStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_stats, parent, false);
        return new TeamStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamStatsViewHolder holder, int position) {
        TeamStats curTeamStats = teamStats.get(position);


        int teamNumber = curTeamStats.getTeam().getTeamNumber();
        String teamName = curTeamStats.getTeam().getTeamName();

        double avgPoints = curTeamStats.calculateAvgPoints();
        double avgGamePieces = curTeamStats.getAvgGamePieceCount();
        GamePiece mostScoredPiece = curTeamStats.getMostScoredGamePiece();

        holder.teamNumberTextView.setText(String.valueOf(teamNumber));
        holder.teamNameTextView.setText(teamName);
        holder.avgPointsTextView.setText("ממוצע נקודות: " + String.format("%.1f", Double.isNaN(avgPoints) ? 0 : avgPoints));
        holder.avgGamePiecesTextView.setText("ממוצע חלקי משחק: " + String.format("%.1f", Double.isNaN(avgGamePieces) ? 0 : avgGamePieces));

        String mostScoredText = mostScoredPiece != null
                ? "הגובה הממוצע: " + mostScoredPiece.name()
                : "הגובה הממוצע: אין מידע";
        holder.mostScoredTextView.setText(mostScoredText);

        holder.gamesPlayedTextView.setText("כמות משחקים: " + curTeamStats.getGamesPlayed());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TeamProfileActivity.class);
            intent.putExtra(TeamProfileActivity.EXTRA_TEAM_NUMBER, curTeamStats.getTeam().getTeamNumber());
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return teamStats.size();
    }

    public void updateData(ArrayList<TeamStats> newData) {
        this.teamStats = newData;
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