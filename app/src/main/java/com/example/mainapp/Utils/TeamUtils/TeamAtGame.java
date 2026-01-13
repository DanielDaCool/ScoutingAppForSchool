package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.GamePiece;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamAtGame implements Serializable {
    private Team team;
    private List<GamePieceScore> gamePiecesScored;
    private int gameID;
    private Map<String, Integer> gamePieceCount;
    private CLIMB c;



    public TeamAtGame() {
        this.gamePiecesScored = new ArrayList<>();
        this.gamePieceCount = new HashMap<>();
        this.c = CLIMB.DIDNT_TRY;
    }

    public TeamAtGame(Team team, int gameID) {
        this.team = team;
        this.gamePiecesScored = new ArrayList<>();
        this.gameID = gameID;
        this.gamePieceCount = new HashMap<>();
        for (GamePiece g : GamePiece.values()) {
            gamePieceCount.put(g.name(), 0);
        }
        this.c = CLIMB.DIDNT_TRY;

    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getGameID() {
        return this.gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public List<GamePieceScore> getGamePiecesScored() {
        return this.gamePiecesScored;
    }

    public void setGamePiecesScored(List<GamePieceScore> gamePiecesScored) {
        this.gamePiecesScored = gamePiecesScored;
    }

    public Map<String, Integer> getGamePieceCount() {
        return this.gamePieceCount;
    }

    public void setGamePieceCount(Map<String, Integer> gamePieceCount) {
        this.gamePieceCount = gamePieceCount;
    }
    public void setClimb(CLIMB c){
        this.c = c;
    }
    public CLIMB getClimb(){return this.c;}


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

    // FIXED: Add @Exclude so Firebase doesn't try to serialize this
    // Helper method to get HashMap with enum keys for app use
    @Exclude
    public HashMap<GamePiece, Integer> getGamePieceCountAsEnum() {
        HashMap<GamePiece, Integer> result = new HashMap<>();
        if (gamePieceCount != null) {
            for (Map.Entry<String, Integer> entry : gamePieceCount.entrySet()) {
                try {
                    result.put(GamePiece.valueOf(entry.getKey()), entry.getValue());
                } catch (IllegalArgumentException e) {
                    // Skip invalid game piece names
                }
            }
        }
        return result;
    }

    @Exclude
    public int calculatePoints() {
        if (gamePiecesScored == null || gamePiecesScored.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (GamePieceScore g : gamePiecesScored) {
            try {
                GamePiece piece = GamePiece.valueOf(g.getPiece());
                sum += g.isInAuto() ? piece.getAutoPoints() : piece.getTeleopPoints();
            } catch (Exception e) {
                // Skip invalid pieces
            }
        }
        return sum + c.getPoints();
    }

    // Firebase-compatible replacement for Pair
    public static class GamePieceScore implements Serializable {
        private String piece;
        private boolean inAuto;

        public GamePieceScore() {
        }

        public GamePieceScore(String piece, boolean inAuto) {
            this.piece = piece;
            this.inAuto = inAuto;
        }

        public String getPiece() {
            return piece;
        }

        public void setPiece(String piece) {
            this.piece = piece;
        }

        public boolean isInAuto() {
            return inAuto;
        }

        public void setInAuto(boolean inAuto) {
            this.inAuto = inAuto;
        }
    }
}