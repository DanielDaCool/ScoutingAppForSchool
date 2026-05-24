package com.example.mainapp.Utils.DatabaseUtils;

/**
 * Represents the Climb component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
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