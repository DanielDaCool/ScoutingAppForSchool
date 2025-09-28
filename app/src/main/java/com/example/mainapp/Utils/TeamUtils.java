package com.example.mainapp.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TeamUtils {

    public static int getTotalScoredGamePieces(ArrayList<TeamAtGame> allGamesOfTeam){
        int count = 0;
        for(TeamAtGame t : allGamesOfTeam){
            for(GamePiece g : GamePiece.values()){
                count += t.getGamePieceCount().get(g);
            }

        }
        return count;
    }
    public static double getAvgGamePiecePerGame(ArrayList<TeamAtGame> allGamesOfTeam){
        int c = getTotalScoredGamePieces(allGamesOfTeam);
        return (double) c/allGamesOfTeam.size();
    }


    public static GamePiece getMostScoredGamePiece(ArrayList<TeamAtGame> allGamesOfTeam){


        HashMap<GamePiece, Integer> totalGamePiecesScoredCount = new HashMap<>();
        for (GamePiece gameP : GamePiece.values()){
            totalGamePiecesScoredCount.put(gameP, 0);
        }

        for(TeamAtGame t : allGamesOfTeam){
            for(GamePiece g : t.getGamePieceCount().keySet()){
                totalGamePiecesScoredCount.put(g, totalGamePiecesScoredCount.get(g) + t.getGamePieceCount().get(g));
            }
        }

        GamePiece mostScored = GamePiece.L1;
        int count = totalGamePiecesScoredCount.get(GamePiece.L1);
        for(GamePiece gamePiece : totalGamePiecesScoredCount.keySet()){
            if(totalGamePiecesScoredCount.get(gamePiece) > count){
                mostScored = gamePiece;
                count = totalGamePiecesScoredCount.get(gamePiece);
            }
        }
        return mostScored;

    }
}
