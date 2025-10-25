package com.example.mainapp.Utils;

import java.io.Serializable;

public class Team implements Serializable {
    private int teamNumber;
    private String teamName;

    public Team(){}
    // ✅ Full constructor
    public Team(int teamNumber, String teamName) {
        this.teamNumber = teamNumber;
        this.teamName = teamName;
    }

    // ✅ Getters and setters
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