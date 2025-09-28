package com.example.mainapp.Utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class TeamAtGame {
    private final Team team;
    private final boolean isBlue;
    private ArrayList<Pair<GamePiece, Boolean>> gamePiecesScored;
    private final int gameID;
    private HashMap<GamePiece, Integer> gamePieceCount;

    public TeamAtGame(Team team, boolean isBlue, int gameID){
        this.team = team;
        this.isBlue = isBlue;
        this.gamePiecesScored = new ArrayList<Pair<GamePiece, Boolean>>();
        this.gameID = gameID;
        this.gamePieceCount = new HashMap<>();
        for(GamePiece g : GamePiece.values()){
            gamePieceCount.put(g, 0);
        }
    }



    public Team getTeam(){return this.team;}
    public boolean isBlue(){return this.isBlue;}
    public ArrayList<Pair<GamePiece, Boolean>> getGamePiecesScored(){return this.gamePiecesScored;}

    public void addGamePieceScored(GamePiece gamePiece, Boolean isScoredInAuto){


        this.gamePiecesScored.add(new Pair<>(gamePiece, isScoredInAuto));
        this.gamePieceCount.put(gamePiece, gamePieceCount.get(gamePiece) +1);



    }

    public HashMap<GamePiece, Integer> getGamePieceCount(){
        return this.gamePieceCount;
    }
    public int calculatePoints(){
        int sum = 0;
        for(Pair<GamePiece, Boolean> g : gamePiecesScored){
            sum += g.second ? g.first.getAutoPoints() : g.first.getTeleopPoints();
        }
        return sum;
    }


}


