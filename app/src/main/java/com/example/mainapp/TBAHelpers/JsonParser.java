package com.example.mainapp.TBAHelpers;

import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    public static Team parseToTeam(JSONObject ob){
        int num = ob.optInt("team_number", -1);
        String name = ob.optString("nickname", "");
        return new Team(num, name);
    }

    // Helper method to extract team number from "frc5987" format
    private static int extractTeamNumber(String teamKey) {
        if (teamKey != null && teamKey.startsWith("frc")) {
            try {
                return Integer.parseInt(teamKey.substring(3));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
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
                    for (int i = 0; i < Math.min(teamKeys.length(), 3); i++) {
                        String teamKey = teamKeys.getString(i);
                        int teamNum = extractTeamNumber(teamKey);
                        redArr[i] = new Team(teamNum, "Team " + teamNum);
                    }
                }
            }

            // Blue alliance - FIXED: was getting "red" instead of "blue"
            if (alliances.has("blue")) {
                JSONObject blue = alliances.getJSONObject("blue");

                if (blue.has("team_keys")) {
                    JSONArray teamKeys = blue.getJSONArray("team_keys");
                    for (int i = 0; i < Math.min(teamKeys.length(), 3); i++) {
                        String teamKey = teamKeys.getString(i);
                        int teamNum = extractTeamNumber(teamKey);
                        blueArr[i] = new Team(teamNum, "Team " + teamNum);
                    }
                }
            }
        }
        return new Game(blueArr, redArr, number);
    }
}