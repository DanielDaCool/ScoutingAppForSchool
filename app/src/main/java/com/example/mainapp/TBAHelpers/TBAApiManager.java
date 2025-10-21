package com.example.mainapp.TBAHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.Team;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class TBAApiManager {


    private static final String API_KEY = "GagyYX9zQGMokeKeZ21B1Ze5Wu0L1yT99J6Z9bQPjnW8ry2IqWYTPUSgumqy3YZV";
    private static final String BASE_URL = "https://www.thebluealliance.com/api/v3";

    private OkHttpClient client;
    public static TBAApiManager instance;
    private TBAApiManager() {
        this.client = new OkHttpClient();
    }


    public static TBAApiManager getInstance(){
        if(instance == null) instance = new TBAApiManager();
        return  instance;
    }
    // Generic method to fetch data
    private String fetchData(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("X-TBA-Auth-Key", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    // Get teams at an event
    public List<Team> getEventTeams(EVENTS eventKey) throws IOException, JSONException {
        String json = fetchData("/event/" + eventKey + "/teams");
        JSONArray jsonArray = new JSONArray(json);

        List<Team> teams = new ArrayList<Team>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject teamJson = jsonArray.getJSONObject(i);
            teams.add(JsonParser.parseToTeam(teamJson));
        }
        return teams;
    }

    // Get matches at an event
    public ArrayList<Game> getEventGames(EVENTS eventKey) throws IOException, JSONException {
        String json = fetchData("/event/" + eventKey + "/matches");
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<Game> gamesList = new ArrayList<Game>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject gameJson = jsonArray.getJSONObject(i);
            gamesList.add(JsonParser.parseToGame(gameJson));
        }
        return gamesList;
    }

    // Get specific team info
    public Team getTeam(int teamNumber) throws IOException, JSONException {
        String json = fetchData("/team/frc" + teamNumber);
        return JsonParser.parseToTeam(new JSONObject(json));

    }

    // Get specific match info
    public Game getMatch(String matchKey) throws IOException, JSONException {
        String json = fetchData("/match/" + matchKey);
        return JsonParser.parseToGame(new JSONObject(json));
    }
}