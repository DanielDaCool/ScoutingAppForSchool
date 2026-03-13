package com.example.mainapp.Utils.DatabaseUtils;

public class Assignment {
    private int gameNumber;
    private int teamNumber;
    private String key; // format: "gameNumber-teamNumber"  "5-5635"

    public Assignment() {}

    public Assignment(int gameNumber, int teamNumber) {
        this.gameNumber = gameNumber;
        this.teamNumber = teamNumber;
        this.key = gameNumber + "-" + teamNumber;
    }

    public int getGameNumber()  { return gameNumber; }
    public int getTeamNumber()  { return teamNumber; }
    public String getKey()      { return key; }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
        this.key = gameNumber + "-" + teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
        this.key = gameNumber + "-" + teamNumber;
    }

    public void setKey(String key) { this.key = key; }
}