package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.DatabaseUtils.Climb;
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

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getGameID() {
        return this.gameID;
    }


    public List<GamePieceScore> getGamePiecesScored() {
        return this.gamePiecesScored;
    }


    public Map<String, Integer> getGamePieceCount() {
        return this.gamePieceCount;
    }


    public void setClimb(Climb c){
        this.climb = c;
    }
    public Climb getClimb(){return this.climb;}


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


    @Exclude
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

        public int getPoints(){
            GamePiece piece = GamePiece.valueOf(this.piece);
            return this.inAuto ? piece.getAutoPoints() : piece.getTeleopPoints();


        }
        public GamePieceScore(String piece, boolean inAuto) {
            this.piece = piece;
            this.inAuto = inAuto;
        }

        public String getPiece() {
            return piece;
        }


        public boolean isInAuto() {
            return inAuto;
        }

    }
}