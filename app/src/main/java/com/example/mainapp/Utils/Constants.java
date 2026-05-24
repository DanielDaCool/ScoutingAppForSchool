package com.example.mainapp.Utils;

import com.example.mainapp.TBAHelpers.EVENTS;

/**
 * Constants class holds application-wide static constant values,
 * such as Firebase table names and default competition events.
 */
public class Constants {

    public static final String USERS_TABLE_NAME       = "users";
    public static final String TEAMS_TABLE_NAME       = "teams";
    public static final String ASSIGNMENTS_TABLE_NAME = "assignments";


    public static EVENTS DEFAULT_EVENT = EVENTS.values()[0];
}