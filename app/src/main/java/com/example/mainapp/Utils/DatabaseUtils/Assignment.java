package com.example.mainapp.Utils.DatabaseUtils;

/**
 * Assignment class represents a specific match and team combination assigned to a scouter.
 * It uses a composite key based on the game and team numbers for identification in the database.
 */
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

/**
 * Executes the logic associated with the getGameNumber operation.
 * @return the value produced by this method.
 */
    public int getGameNumber()  { return gameNumber; }
/**
 * Executes the logic associated with the getTeamNumber operation.
 * @return the value produced by this method.
 */
    public int getTeamNumber()  { return teamNumber; }
/**
 * Executes the logic associated with the getKey operation.
 * @return the value produced by this method.
 */
    public String getKey()      { return key; }


/**
 * Executes the logic associated with the setTeamNumber operation.
 * @param teamNumber parameter required for this method.
 */
    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
        this.key = gameNumber + "-" + teamNumber;
    }

}