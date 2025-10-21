package com.example.mainapp.Utils;

import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.TBAHelpers.TBAApiManager;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {
    public static String getScouterNameFromID(int scouterID) {
        return "TEST " + Tests.generateRandomNumber(10) ;
    }

//    public static ArrayList<Game> getGames(EVENTS event) {
//        try{
//            return TBAApiManager.getInstance().getEventGames(event);
//
//        }
//        catch (Exception e){
//            return new ArrayList<>();
//        }
//    }

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


