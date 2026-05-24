package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.DatabaseUtils.Climb;
import com.example.mainapp.Utils.GamePiece;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the TeamAtGame component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class TeamAtGame implements Serializable {
    private Team team;
    private List<GamePieceScore> gamePiecesScored;
    private int gameID;
    private Map<String, Integer> gamePieceCount;
    private Climb climb;



    public TeamAtGame() {
        this.gamePiecesScored = new ArrayList<>();
        this.gamePieceCount = new HashMap<>();
        this.climb = Climb.DIDNT_TRY;
    }

    public TeamAtGame(Team team, int gameID) {
        this.team = team;
        this.gamePiecesScored = new ArrayList<>();
        this.gameID = gameID;
        this.gamePieceCount = new HashMap<>();
        for (GamePiece g : GamePiece.values()) {
            gamePieceCount.put(g.name(), 0);
        }
        this.climb = Climb.DIDNT_TRY;

    }

/**
 * Executes the logic associated with the getTeam operation.
 * @return the value produced by this method.
 */
    public Team getTeam() {
        return this.team;
    }

/**
 * Executes the logic associated with the setTeam operation.
 * @param team parameter required for this method.
 */
    public void setTeam(Team team) {
        this.team = team;
    }

/**
 * Executes the logic associated with the getGameID operation.
 * @return the value produced by this method.
 */
    public int getGameID() {
        return this.gameID;
    }


/**
 * Executes the logic associated with the getGamePiecesScored operation.
 * @return the value produced by this method.
 */
    public List<GamePieceScore> getGamePiecesScored() {
        return this.gamePiecesScored;
    }


    public Map<String, Integer> getGamePieceCount() {
        return this.gamePieceCount;
    }


/**
 * Executes the logic associated with the setClimb operation.
 * @param c parameter required for this method.
 */
    public void setClimb(Climb c){
        this.climb = c;
    }
/**
 * Executes the logic associated with the getClimb operation.
 * @return the value produced by this method.
 */
    public Climb getClimb(){return this.climb;}


/**
 * Executes the logic associated with the addGamePieceScored operation.
 * @param gamePiece parameter required for this method.
 * @param isScoredInAuto parameter required for this method.
 */
    public void addGamePieceScored(GamePiece gamePiece, Boolean isScoredInAuto) {
        if (this.gamePiecesScored == null) {
            this.gamePiecesScored = new ArrayList<>();
        }
        if (this.gamePieceCount == null) {
            this.gamePieceCount = new HashMap<>();
        }

        this.gamePiecesScored.add(new GamePieceScore(gamePiece.name(), isScoredInAuto));
        String key = gamePiece.name();
        this.gamePieceCount.put(key, gamePieceCount.getOrDefault(key, 0) + 1);
    }


    public void addGamePieceScored(GamePieceScore gamePieceScore, int count){
        for(int i = 0; i < count; i++) addGamePieceScored(GamePiece.getGamePieceFromString(gamePieceScore.getPiece()), gamePieceScore.isInAuto());
    }


    @Exclude
/**
 * Executes the logic associated with the calculatePoints operation.
 * @return the value produced by this method.
 */
    public int calculatePoints() {
        if (gamePiecesScored == null || gamePiecesScored.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (GamePieceScore g : gamePiecesScored) {
            sum+= g.getPoints();
        }
        return sum + climb.getPoints();
    }

    public static class GamePieceScore implements Serializable {
        private String piece;
        private boolean inAuto;

        public GamePieceScore() {
        }

/**
 * Executes the logic associated with the getPoints operation.
 * @return the value produced by this method.
 */
        public int getPoints(){
            GamePiece piece = GamePiece.valueOf(this.piece);
            return this.inAuto ? piece.getAutoPoints() : piece.getTeleopPoints();


        }
        public GamePieceScore(String piece, boolean inAuto) {
            this.piece = piece;
            this.inAuto = inAuto;
        }
        public  GamePieceScore(GamePiece g, boolean inAuto){
            this(g.name(), inAuto);
        }

/**
 * Executes the logic associated with the getPiece operation.
 * @return the value produced by this method.
 */
        public String getPiece() {
            return piece;
        }


/**
 * Executes the logic associated with the isInAuto operation.
 * @return the value produced by this method.
 */
        public boolean isInAuto() {
            return inAuto;
        }

    }
}