package com.example.mainapp.TBAHelpers;


import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    public static Team parseToTeam(JSONObject ob){
        int num = ob.optInt("team_number", -1);
        String name = ob.optString("name", "");
        return new Team(num, name);
    }
    public static Game parseToGame(JSONObject ob) throws JSONException {
        int number = ob.optInt("match_number", -1);
        Team[] redArr = new Team[3];
        Team[] blueArr = new Team[3];

        if (ob.has("alliances")) {
            JSONObject alliances = ob.getJSONObject("alliances");

            // Red alliance
            if (alliances.has("red")) {
                JSONObject red = alliances.getJSONObject("red");


                if (red.has("team_keys")) {
                    JSONArray teamKeys = red.getJSONArray("team_keys");
                    for (int i = 0; i < teamKeys.length(); i++) {
                        redArr[i] = parseToTeam(teamKeys.getJSONObject(i));
                    }
                }
            }
            if (alliances.has("blue")) {
                JSONObject blue = alliances.getJSONObject("red");


                if (blue.has("team_keys")) {
                    JSONArray teamKeys = blue.getJSONArray("team_keys");
                    for (int i = 0; i < teamKeys.length(); i++) {
                        blueArr[i] = parseToTeam(teamKeys.getJSONObject(i));
                    }
                }
            }
        }
        return new Game(blueArr, redArr,number);
    }

}
