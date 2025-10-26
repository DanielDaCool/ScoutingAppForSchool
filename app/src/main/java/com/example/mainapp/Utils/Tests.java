package com.example.mainapp.Utils;

import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;

import java.util.ArrayList;

public class Tests {

    public static String generateTeamName(){
        return "Name " + generateRandomNumber(10000);
    }
    public static ArrayList<TeamAtGame> generateTeamAtGame(){
        Team t = new Team(generateRandomNumber(10000), "NAME " + generateRandomNumber(1000));
        ArrayList<TeamAtGame> perTeam = new ArrayList<TeamAtGame>();
        for(int i = 0; i < 60; i++){

            TeamAtGame g = new TeamAtGame(t , i);
            generateGamePiecesScored(g);
            perTeam.add(g);
        }
        return perTeam;
    }
    public static ArrayList<ArrayList<TeamAtGame>> generateAllGamesOfTeams(){
        ArrayList<ArrayList<TeamAtGame>> res = new ArrayList<>();
        for(int j = 0; j < 100; j++){

            Team t = new Team(generateRandomNumber(10000), "NAME " + generateRandomNumber(1000));
            ArrayList<TeamAtGame> perTeam = new ArrayList<TeamAtGame>();
            for(int i = 0; i < 60; i++){

                TeamAtGame g = new TeamAtGame(t,  i);
                generateGamePiecesScored(g);
                perTeam.add(g);
            }
            res.add(perTeam);
        }
        return res;
    }

    private static void generateGamePiecesScored(TeamAtGame g){
        int s = generateRandomNumber(30);
        for(int i = 0; i < s; i++){
            g.addGamePieceScored(generateRandomGamePiece(), generateRandomNumber(2) > 1);
        }
    }

    private static GamePiece generateRandomGamePiece(){
        int n = generateRandomNumber(4);
        switch (n){
            case 0:
                return GamePiece.L1;

            case 1:
                return GamePiece.L2;

            case 2:
                return GamePiece.L3;
            case 3:
                return GamePiece.L4;
            default:
                return GamePiece.L1;

        }
    }
    public static int generateRandomNumber(int maxValue) {
        return (int) (Math.random() * maxValue) + 1;
    }

    public static ArrayList<Game> generateGames() {
        ArrayList<Game> l = new ArrayList<Game>();

        for (int i = 0; i < 300; i++) {
            l.add(generateGame(i+1));
        }
        return l;
    }


    public static Game generateGame(int id) {
        return new Game(generateAlliance(), generateAlliance(), id, generateRandomNumber(100));
    }

    public static Team[] generateAlliance() {
        Team[] t = new Team[3];

        for (int i = 0; i < t.length; i++) {
            t[i] = new Team(generateRandomNumber(1000), "TBD");
        }

        return t;
    }


}
