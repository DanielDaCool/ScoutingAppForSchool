package com.example.mainapp.Utils;

import android.content.Context;

import com.example.mainapp.TBAHelpers.EVENTS;

public class Constants {

    public static final String USERS_TABLE_NAME       = "users";
    public static final String TEAMS_TABLE_NAME       = "teams";
    public static final String ASSIGNMENTS_TABLE_NAME = "assignments";


    public static EVENTS getCurrentEvent(Context context) {
        EVENTS district = SharedPrefHelper.getInstance(context).getCurrentDistrict();
        return district != null ? district : EVENTS.values()[0];
    }

    // Static fallback for places without a Context
    public static EVENTS CURRENT_EVENT_ON_APP = EVENTS.values()[0];
}