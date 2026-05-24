package com.example.mainapp.Utils;

import com.example.mainapp.Utils.TeamUtils.Team;

import java.util.Arrays;

/**
 * Represents the Game component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class Game {
    private final Team[] blueAlliance;
    private final Team[] redAlliance;
    private final int gameNumber;


    public Game(Team[] blueAlliance, Team[] redAlliance, int gameNumber) {
        this.blueAlliance = blueAlliance;
        this.redAlliance = redAlliance;
        this.gameNumber = gameNumber;
    }


/**
 * Executes the logic associated with the getGameNumber operation.
 * @return the value produced by this method.
 */
    public int getGameNumber() {
        return this.gameNumber;
    }

/**
 * Executes the logic associated with the getBlueAlliance operation.
 * @return the value produced by this method.
 */
    public Team[] getBlueAlliance() {
        return this.blueAlliance;
    }

/**
 * Executes the logic associated with the getRedAlliance operation.
 * @return the value produced by this method.
 */
    public Team[] getRedAlliance() {
        return this.redAlliance;
    }
/**
 * Executes the logic associated with the getPlayingTeamsNumbers operation.
 * @return the value produced by this method.
 */
    public int[] getPlayingTeamsNumbers(){
        int[] arr = new int[6];
        Arrays.fill(arr, 0);

        for (int i = 0; i < 3; i++){
            arr[i] = this.redAlliance[i].getTeamNumber();
        }
        for(int j = 3; j < 6; j++){
            arr[j] = this.blueAlliance[j-3].getTeamNumber();
        }
        return arr;
    }

/**
 * Executes the logic associated with the getGameTitle operation.
 * @return the value produced by this method.
 */
    public String getGameTitle() {
        return "משחק " + this.gameNumber;
    }

/**
 * Executes the logic associated with the getDescription operation.
 * @return the value produced by this method.
 */
    public String getDescription(){
        return getTeamsPlaying();
    }
/**
 * Executes the logic associated with the getTeamsPlaying operation.
 * @return the value produced by this method.
 */
    private String getTeamsPlaying() {
        StringBuilder blueAllianceString = new StringBuilder();
        StringBuilder redAllianceString = new StringBuilder();

        for (int i = 0; i < this.redAlliance.length; i++) {
            blueAllianceString.append(this.blueAlliance[i].getTeamNumber()).append(", ");
            redAllianceString.append(this.redAlliance[i].getTeamNumber()).append(", ");
        }


        return blueAllianceString.deleteCharAt(blueAllianceString.length() - 2) + "VS " + redAllianceString.deleteCharAt(redAllianceString.length() - 2);
    }

}