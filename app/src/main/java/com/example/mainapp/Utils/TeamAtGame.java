package com.example.mainapp.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamAtGame implements Serializable {
    private Team team;
    private boolean isBlue;
    private List<GamePieceScore> gamePiecesScored;
    private int gameID;
    private Map<String, Integer> gamePieceCount;

    // Empty constructor for Firebase
    public TeamAtGame() {
    }

    public TeamAtGame(Team team, boolean isBlue, int gameID) {
        this.team = team;
        this.isBlue = isBlue;
        this.gamePiecesScored = new ArrayList<>();
        this.gameID = gameID;
        this.gamePieceCount = new HashMap<>();
        for (GamePiece g : GamePiece.values()) {
            gamePieceCount.put(g.name(), 0);
        }
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean isBlue() {
        return this.isBlue;
    }

    public void setBlue(boolean blue) {
        this.isBlue = blue;
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

    public void addGamePieceScored(GamePiece gamePiece, Boolean isScoredInAuto) {
        this.gamePiecesScored.add(new GamePieceScore(gamePiece.name(), isScoredInAuto));
        String key = gamePiece.name();
        this.gamePieceCount.put(key, gamePieceCount.getOrDefault(key, 0) + 1);
    }

    // Helper method to get HashMap with enum keys for app use
    public HashMap<GamePiece, Integer> getGamePieceCountAsEnum() {
        HashMap<GamePiece, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : gamePieceCount.entrySet()) {
            result.put(GamePiece.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public int calculatePoints() {
        int sum = 0;
        for (GamePieceScore g : gamePiecesScored) {
            GamePiece piece = GamePiece.valueOf(g.getPiece());
            sum += g.isInAuto() ? piece.getAutoPoints() : piece.getTeleopPoints();
        }
        return sum;
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