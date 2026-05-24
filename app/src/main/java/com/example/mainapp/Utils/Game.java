package com.example.mainapp.Utils;

import com.example.mainapp.Utils.TeamUtils.Team;

import java.util.Arrays;

/**
 * Game class represents a single match between two alliances (Red and Blue).
 * Each alliance consists of three teams.
 */
public class Game {
    private final Team[] blueAlliance;
    private final Team[] redAlliance;
    private final int gameNumber;


    /**
     * Constructs a new Game with specified alliances and match number.
     * @param blueAlliance Array of 3 teams on the blue alliance.
     * @param redAlliance Array of 3 teams on the red alliance.
     * @param gameNumber The match number.
     */
    public Game(Team[] blueAlliance, Team[] redAlliance, int gameNumber) {
        this.blueAlliance = blueAlliance;
        this.redAlliance = redAlliance;
        this.gameNumber = gameNumber;
    }


    /**
     * @return The match number of this game.
     */
    public int getGameNumber() {
        return this.gameNumber;
    }

    /**
     * @return An array of teams participating in the blue alliance.
     */
    public Team[] getBlueAlliance() {
        return this.blueAlliance;
    }

    /**
     * @return An array of teams participating in the red alliance.
     */
    public Team[] getRedAlliance() {
        return this.redAlliance;
    }

    /**
     * Returns an array containing all team numbers in the match.
     * Indices 0-2 are Red alliance, 3-5 are Blue alliance.
     * @return Array of 6 team numbers.
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
     * @return A localized string title for the game (e.g., "משחק 1").
     */
    public String getGameTitle() {
        return "משחק " + this.gameNumber;
    }

    /**
     * @return A string description of the teams playing in this match.
     */
    public String getDescription(){
        return getTeamsPlaying();
    }

    /**
     * Helper method to format the alliance team numbers into a string.
     * @return Formatted string "Red Teams VS Blue Teams".
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