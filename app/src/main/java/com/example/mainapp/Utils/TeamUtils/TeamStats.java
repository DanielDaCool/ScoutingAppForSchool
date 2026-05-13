package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.DatabaseUtils.Climb;
import com.example.mainapp.Utils.GamePiece;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@IgnoreExtraProperties
public class TeamStats implements Serializable {

    private ArrayList<TeamAtGame> allGames;
    private Team team;

    public TeamStats() {
        this.allGames = new ArrayList<>();
    }

    public TeamStats(Team t) {
        this.team = t;
        this.allGames = new ArrayList<>();
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team t) {
        this.team = t;
    }

    public List<TeamAtGame> getAllGames() {
        return this.allGames;
    }


    public void addGame(TeamAtGame g) {
        allGames.add(g);
    }

    public double calculateAvgClimbPerGame(){
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;
        }

        int count = 0;
        for (TeamAtGame t : allGames){
            if(t.getClimb() != null && t.getClimb() != Climb.DIDNT_TRY && t.getClimb() != Climb.FAILED) count++;
        }
        return  (double) count / allGames.size();
    }
    public double calculateAvgPoints() {
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (TeamAtGame t : allGames) {
            total += t.calculatePoints();
        }
        return (double) total / getGamesPlayed();
    }

    public int getGamesPlayed() {
        return allGames == null ? 0 : allGames.size();
    }

    public GamePiece getMostScoredGamePiece() {
        if (allGames == null || allGames.isEmpty()) {
            return null;
        }
        return TeamUtils.getMostScoredGamePiece(allGames);
    }


    public double getAvgGamePieceCount() {
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;
        }
        return TeamUtils.getAvgGamePieceCountPerGame(allGames);
    }
}