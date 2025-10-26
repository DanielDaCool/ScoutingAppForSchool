package com.example.mainapp.Utils;

import com.example.mainapp.TBAHelpers.EVENTS;

import java.sql.Time;

public class Constants {
    public static final EVENTS CURRENT_EVENT_ON_APP = EVENTS.DISTRICT_4;
    public static final String USERS_TABLE_NAME = "users";
    public static final String GAMES_TABLE_NAME = "teams";
    public static int STATS_CHECK_INTERVAL = 10 * 1000; //first num in seconds but need in milliseconds
}
