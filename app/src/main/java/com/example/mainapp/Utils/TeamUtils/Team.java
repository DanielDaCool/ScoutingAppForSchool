package com.example.mainapp.Utils.TeamUtils;

import java.io.Serializable;

public class Team implements Serializable {
    public static Team kDefaultTeam = new Team(-1, "Error");
    private int teamNumber;
    private String teamName;

    public Team(){}
    public Team(int teamNumber, String teamName) {
        this.teamNumber = teamNumber;
        this.teamName = teamName;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}