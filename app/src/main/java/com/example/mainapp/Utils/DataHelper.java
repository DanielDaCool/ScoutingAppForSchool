package com.example.mainapp.Utils;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {
    public static String getScouterNameFromID(int scouterID){
        return "TEST";
    }
    public static ArrayList<Game> getGames(){ //need to decide if using API or manually writing in constants the games
        return new ArrayList<Game>(Tests.generateGames());
    }


}
