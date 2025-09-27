package com.example.mainapp.Utils;

import android.os.Build;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Tests {
    public static int generateRandomNumber(int maxValue) {
        return (int) (Math.random() * maxValue) + 1;
    }

    public static ArrayList<Game> generateGames() {
        ArrayList<Game> l = new ArrayList<Game>();

        for (int i = 0; i < 60; i++) {
            l.add(generateGame(i));
        }
        return l;
    }

    public static Game generateGame(int id) {
        return new Game(generateAlliance(), generateAlliance(), id, generateRandomNumber(100));
    }

    public static Team[] generateAlliance() {
        Team[] t = new Team[3];

        for (int i = 0; i < t.length; i++) {
            t[i] = new Team(generateRandomNumber(10000), "TBD");
        }

        return t;
    }


}
