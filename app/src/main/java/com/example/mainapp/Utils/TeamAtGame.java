package com.example.mainapp.Utils;

import android.util.Pair;

import java.util.ArrayList;

public class TeamAtGame {
    private final Team team;
    private final boolean isBlue;
    private ArrayList<Pair<GamePiece, Boolean>> gamePiecesScored;

    public TeamAtGame(Team team, boolean isBlue){
        this.team = team;
        this.isBlue = isBlue;
        this.gamePiecesScored = new ArrayList<Pair<GamePiece, Boolean>>();
    }


    public Team getTeam(){return this.team;}
    public boolean isBlue(){return this.isBlue;}
    public ArrayList<Pair<GamePiece, Boolean>> getGamePiecesScored(){return this.gamePiecesScored;}

    public void addGamePieceScored(GamePiece gamePiece, Boolean isScoredInAuto){this.gamePiecesScored.add(new Pair<>(gamePiece, isScoredInAuto));}
    public int calculatePoints(){
        int sum = 0;
        for(Pair<GamePiece, Boolean> g : gamePiecesScored){
            sum += g.second ? g.first.getAutoPoints() : g.first.getTeleopPoitns();
        }
        return sum;
    }


}


