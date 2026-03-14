package com.example.mainapp.Utils.DatabaseUtils;

import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

public class AppCache {
    private static AppCache instance;


    private ArrayList<TeamStats> allTeamStats;
    private ArrayList<Game> gamesList;
    private ArrayList<Team> israeliTeams;
    private long teamCount;
    private int totalGames;
    private Team[] teamsAtEvent;
    private  AppCache(){}

    public ArrayList<TeamStats> getAllTeamStats() { return allTeamStats; }
    public void setAllTeamStats(ArrayList<TeamStats> allTeamStats) { this.allTeamStats = allTeamStats; }

    // ── Games list ───────────────────────────────────────
    public ArrayList<Game> getGamesList() { return gamesList; }
    public void setGamesList(ArrayList<Game> gamesList) { this.gamesList = gamesList; }

    // ── Israeli teams ─────────────────────────────────────
    public ArrayList<Team> getIsraeliTeams() { return israeliTeams; }
    public void setIsraeliTeams(ArrayList<Team> israeliTeams) { this.israeliTeams = israeliTeams; }

    // ── Counts ────────────────────────────────────────────
    public long getTeamCount() { return teamCount; }
    public void setTeamCount(long teamCount) { this.teamCount = teamCount; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }


    public Team[] getTeamsAtEvent(){
        return this.teamsAtEvent;
    }
    public void setTeamsAtEvent(Team[] teamsAtEvent){
        this.teamsAtEvent = teamsAtEvent;
    }


    public boolean isCacheEmpty() {
        return teamsAtEvent == null && allTeamStats == null && gamesList == null;
    }
    public static synchronized AppCache getInstance(){
        if(instance == null) instance = new AppCache();
        return instance;
    }
}
