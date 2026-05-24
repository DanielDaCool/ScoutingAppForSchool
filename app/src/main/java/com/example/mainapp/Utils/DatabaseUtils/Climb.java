package com.example.mainapp.Utils.DatabaseUtils;

/**
 * Climb enum represents the possible end-game states for a robot.
 * Each state (High, Low, Failed, Did Not Try) is associated with a specific point value.
 */
public enum Climb {
    HIGH(12),
    LOW(6),
    FAILED(0),
    DIDNT_TRY(0);
    int points;
    Climb(int points){
        this.points = points;
    }

/**
 * Executes the logic associated with the getPoints operation.
 * @return the value produced by this method.
 */
    public int getPoints() {
        return points;
    }

}