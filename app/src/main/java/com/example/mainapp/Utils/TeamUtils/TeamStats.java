package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.DatabaseUtils.Climb;
import com.example.mainapp.Utils.GamePiece;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@IgnoreExtraProperties
/**
 * TeamStats aggregates performance data for a specific team across all matches played.
 * It provides methods to calculate averages for points, climbs, and game piece scoring.
 */
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

/**
 * Executes the logic associated with the getTeam operation.
 * @return the value produced by this method.
 */
    public Team getTeam() {
        return this.team;
    }

/**
 * Executes the logic associated with the setTeam operation.
 * @param t parameter required for this method.
 */
    public void setTeam(Team t) {
        this.team = t;
    }

/**
 * Executes the logic associated with the getAllGames operation.
 * @return the value produced by this method.
 */
    public List<TeamAtGame> getAllGames() {
        return this.allGames;
    }


/**
 * Executes the logic associated with the addGame operation.
 * @param g parameter required for this method.
 */
    public void addGame(TeamAtGame g) {
        allGames.add(g);
    }

/**
 * Executes the logic associated with the calculateAvgClimbPerGame operation.
 * @return the value produced by this method.
 */
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
/**
 * Executes the logic associated with the calculateAvgPoints operation.
 * @return the value produced by this method.
 */
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

/**
 * Executes the logic associated with the getGamesPlayed operation.
 * @return the value produced by this method.
 */
    public int getGamesPlayed() {
        return allGames == null ? 0 : allGames.size();
    }

/**
 * Executes the logic associated with the getMostScoredGamePiece operation.
 * @return the value produced by this method.
 */
    public GamePiece getMostScoredGamePiece() {
        if (allGames == null || allGames.isEmpty()) {
            return null;
        }
        return TeamUtils.getMostScoredGamePiece(allGames);
    }


/**
 * Executes the logic associated with the getAvgGamePieceCount operation.
 * @return the value produced by this method.
 */
    public double getAvgGamePieceCount() {
        if (allGames == null || allGames.isEmpty()) {
            return 0.0;
        }
        return TeamUtils.getAvgGamePieceCountPerGame(allGames);
    }
}