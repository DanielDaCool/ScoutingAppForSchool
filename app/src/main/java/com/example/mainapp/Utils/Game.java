package com.example.mainapp.Utils;

import java.util.Arrays;

public class Game {
    private final Team[] blueAlliance;
    private final Team[] redAlliance;
    private final int gameNumber;
    private final int scouterID;

    public Game(Team[] blueAlliance, Team[] redAlliance, int gameNumber, int scouterID) {
        this.blueAlliance = blueAlliance;
        this.redAlliance = redAlliance;
        this.gameNumber = gameNumber;
        this.scouterID = scouterID;
    }
    public Game(Team[] blueAlliance, Team[] redAlliance, int gameNumber) {
        this.blueAlliance = blueAlliance;
        this.redAlliance = redAlliance;
        this.gameNumber = gameNumber;
        this.scouterID = -1;
    }


    public int getScouterID() {
        return this.scouterID;
    }

    public int getGameNumber() {
        return this.gameNumber;
    }

    public Team[] getBlueAlliance() {
        return this.blueAlliance;
    }

    public Team[] getRedAlliance() {
        return this.redAlliance;
    }
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

    public String getGameTitle() {
        return "משחק " + this.gameNumber;
    }

    public String getDescription(){
        return getTeamsPlaying();
    }
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
