package com.example.mainapp.Utils.TeamUtils;

import com.example.mainapp.Utils.GamePiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TeamUtils {

    public static int getTotalScoredGamePieces(ArrayList<TeamAtGame> allGamesOfTeam) {
        if (allGamesOfTeam == null || allGamesOfTeam.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (TeamAtGame t : allGamesOfTeam) {
            if (t == null || t.getGamePieceCount() == null) {
                continue;
            }

            // FIXED: Use String key instead of GamePiece enum
            for (GamePiece g : GamePiece.values()) {
                Integer pieceCount = t.getGamePieceCount().get(g.name());
                if (pieceCount != null) {
                    count += pieceCount;
                }
            }
        }
        return count;
    }

    public static double getAvgGamePiecePerGame(ArrayList<TeamAtGame> allGamesOfTeam) {
        if (allGamesOfTeam == null || allGamesOfTeam.isEmpty()) {
            return 0.0;
        }
        int c = getTotalScoredGamePieces(allGamesOfTeam);
        return (double) c / allGamesOfTeam.size();
    }

    public static boolean ContainsTeam(Team[] arr, int teamNumber) {
        if (arr == null) {
            return false;
        }
        for (Team t : arr) {
            if (t != null && t.getTeamNumber() == teamNumber) {
                return true;
            }
        }
        return false;
    }

    public static boolean ContainsTeam(int[] arr, int teamNumber) {
        if (arr == null) {
            return false;
        }
        for (int a : arr) {
            if (a == teamNumber) {
                return true;
            }
        }
        return false;
    }

    public static GamePiece getMostScoredGamePiece(ArrayList<TeamAtGame> allGamesOfTeam) {
        if (allGamesOfTeam == null || allGamesOfTeam.isEmpty()) {
            return GamePiece.L1; // Return default
        }

        HashMap<GamePiece, Integer> totalGamePiecesScoredCount = new HashMap<>();
        for (GamePiece gameP : GamePiece.values()) {
            totalGamePiecesScoredCount.put(gameP, 0);
        }

        for (TeamAtGame t : allGamesOfTeam) {
            if (t == null || t.getGamePieceCount() == null) {
                continue;
            }

            for (String str : t.getGamePieceCount().keySet()) {
                try {
                    GamePiece g = GamePiece.getGamePieceFromString(str);
                    Integer currentCount = totalGamePiecesScoredCount.get(g);
                    Integer gameCount = t.getGamePieceCount().get(str);

                    if (currentCount != null && gameCount != null) {
                        totalGamePiecesScoredCount.put(g, currentCount + gameCount);
                    }
                } catch (Exception e) {
                    // Skip invalid game piece strings
                    continue;
                }
            }
        }

        GamePiece mostScored = GamePiece.L1;
        int maxCount = totalGamePiecesScoredCount.get(GamePiece.L1);

        for (Map.Entry<GamePiece, Integer> entry : totalGamePiecesScoredCount.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > maxCount) {
                mostScored = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return mostScored;
    }
}