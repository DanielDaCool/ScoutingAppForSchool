package com.example.mainapp.Utils;

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

    public String getGameTitle() {
        return "משחק " + this.gameNumber;
    }

    public String getDescription(){
        return getTeamsPlaying() +  "\n" + " סקאוטר: " + DataHelper.getScouterNameFromID(scouterID);
    }
    private String getTeamsPlaying() {
        StringBuilder blueAlliance = new StringBuilder();
        StringBuilder redAlliance = new StringBuilder();

        for (int i = 0; i < this.redAlliance.length; i++) {
            blueAlliance.append(this.blueAlliance[i].teamNumber()).append(", ");
            redAlliance.append(this.redAlliance[i].teamNumber()).append(", ");
        }

        return blueAlliance.deleteCharAt(redAlliance.length() - 2) + "VS " + redAlliance.deleteCharAt(redAlliance.length() - 2);
    }

}
