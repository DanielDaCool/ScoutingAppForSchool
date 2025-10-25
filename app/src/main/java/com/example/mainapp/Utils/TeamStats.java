package com.example.mainapp.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public void setAllGames(ArrayList<TeamAtGame> allGames) {
        this.allGames = allGames;
    }

    public void addGame(TeamAtGame g) {
        allGames.add(g);
    }

    public double calculateAvgPoints() {
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;  // ✅ Return 0 instead of NaN
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
            return null;  // Or return a default GamePiece
        }
        return TeamUtils.getMostScoredGamePiece(allGames);
    }

    public int getTotalGamePieceCount() {
        if (allGames == null || allGames.isEmpty()) {
            return 0;
        }
        return TeamUtils.getTotalScoredGamePieces(allGames);
    }

    public double getAvgGamePieceCount() {
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;  // ✅ Return 0 instead of NaN
        }
        return TeamUtils.getAvgGamePiecePerGame(allGames);
    }
}