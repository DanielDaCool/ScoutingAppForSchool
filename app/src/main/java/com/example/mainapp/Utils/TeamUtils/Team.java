package com.example.mainapp.Utils.TeamUtils;

import java.io.Serializable;

/**
 * Represents the Team component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class Team implements Serializable {
    public static Team kDefaultTeam = new Team(-1, "Error");
    private int teamNumber;
    private String teamName;

    public Team(){}
    public Team(int teamNumber, String teamName) {
        this.teamNumber = teamNumber;
        this.teamName = teamName;
    }

/**
 * Executes the logic associated with the getTeamNumber operation.
 * @return the value produced by this method.
 */
    public int getTeamNumber() {
        return teamNumber;
    }

/**
 * Executes the logic associated with the setTeamNumber operation.
 * @param teamNumber parameter required for this method.
 */
    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

/**
 * Executes the logic associated with the getTeamName operation.
 * @return the value produced by this method.
 */
    public String getTeamName() {
        return teamName;
    }

}