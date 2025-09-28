package com.example.mainapp.Utils;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {
    public static String getScouterNameFromID(int scouterID) {
        return "TEST";
    }

    public static ArrayList<Game> getGames() { //need to decide if using API or manually writing in constants the games
        return new ArrayList<Game>(Tests.generateGames());
    }

    public static double getAvgPoints(int teamNumber) {

        //ArrayList<TeamAtGame> allGames = getGames() from DB
        ArrayList<TeamAtGame> allGames = Tests.generateTeamAtGame();

        double sum = 0;
        int c = 0;
        for(TeamAtGame g : allGames){
            sum += g.calculatePoints();
            c++;
        }
        if(c == 0) return 0;
        return (double) sum/c;

    }
    public static String getTeamNameFromNumber(int teamNumber){
        //call to db to find team name (can be a constant in Constants)

        return Tests.generateTeamName();
    }


}


