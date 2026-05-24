package com.example.mainapp.Utils.DatabaseUtils;

import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

/**
 * Represents the AppCache component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class AppCache {
    private static AppCache instance;


    private ArrayList<TeamStats> allTeamStats;
    private ArrayList<Game> gamesList;
    private ArrayList<Team> israeliTeams;
    private long teamCount;
    private int totalGames;
    private Team[] teamsAtEvent;
    private  AppCache(){}

/**
 * Executes the logic associated with the getAllTeamStats operation.
 * @return the value produced by this method.
 */
    public ArrayList<TeamStats> getAllTeamStats() { return allTeamStats; }
/**
 * Executes the logic associated with the setAllTeamStats operation.
 * @param allTeamStats parameter required for this method.
 */
    public void setAllTeamStats(ArrayList<TeamStats> allTeamStats) { this.allTeamStats = allTeamStats; }

/**
 * Executes the logic associated with the getGamesList operation.
 * @return the value produced by this method.
 */
    public ArrayList<Game> getGamesList() { return gamesList; }
/**
 * Executes the logic associated with the setGamesList operation.
 * @param gamesList parameter required for this method.
 */
    public void setGamesList(ArrayList<Game> gamesList) { this.gamesList = gamesList; }

/**
 * Executes the logic associated with the getIsraeliTeams operation.
 * @return the value produced by this method.
 */
    public ArrayList<Team> getIsraeliTeams() { return israeliTeams; }
/**
 * Executes the logic associated with the setIsraeliTeams operation.
 * @param israeliTeams parameter required for this method.
 */
    public void setIsraeliTeams(ArrayList<Team> israeliTeams) { this.israeliTeams = israeliTeams; }

/**
 * Executes the logic associated with the getTeamCount operation.
 * @return the value produced by this method.
 */
    public long getTeamCount() { return teamCount; }
/**
 * Executes the logic associated with the setTeamCount operation.
 * @param teamCount parameter required for this method.
 */
    public void setTeamCount(long teamCount) { this.teamCount = teamCount; }

/**
 * Executes the logic associated with the getTotalGames operation.
 * @return the value produced by this method.
 */
    public int getTotalGames() { return totalGames; }
/**
 * Executes the logic associated with the setTotalGames operation.
 * @param totalGames parameter required for this method.
 */
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }


/**
 * Executes the logic associated with the getTeamsAtEvent operation.
 * @return the value produced by this method.
 */
    public Team[] getTeamsAtEvent(){
        return this.teamsAtEvent;
    }
/**
 * Executes the logic associated with the setTeamsAtEvent operation.
 * @param teamsAtEvent parameter required for this method.
 */
    public void setTeamsAtEvent(Team[] teamsAtEvent){
        this.teamsAtEvent = teamsAtEvent;
    }


    public static synchronized AppCache getInstance(){
        if(instance == null) instance = new AppCache();
        return instance;
    }
}