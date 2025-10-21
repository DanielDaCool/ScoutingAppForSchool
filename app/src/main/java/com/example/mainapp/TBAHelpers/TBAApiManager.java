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
    public void getEventGames(EVENTS eventKey, GameCallback callback) {
        new Thread(() -> {
            try {
                String json = fetchData("/event/" + eventKey + "/matches");
                System.out.println("JSON Response length: " + json.length());

                JSONArray jsonArray = new JSONArray(json);
                ArrayList<Game> gamesList = new ArrayList<>();

                System.out.println("Total matches in JSON: " + jsonArray.length());

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject gameJson = jsonArray.getJSONObject(i);
                    String compLevel = gameJson.optString("comp_level", "");

                    System.out.println("Match " + i + " comp_level: " + compLevel +
                            ", match_number: " + gameJson.optInt("match_number", -1));

                    if (compLevel.equals("qm")) {
                        try {
                            Game game = JsonParser.parseToGame(gameJson);
                            if (game != null) {
                                gamesList.add(game);
                                System.out.println("Added qual match #" + game.getGameNumber());
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing game " + i + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                // Sort games by match number
                gamesList.sort((g1, g2) -> Integer.compare(g1.getGameNumber(), g2.getGameNumber()));

                System.out.println("Final games list size: " + gamesList.size());
                callback.onSuccess(gamesList);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }

    // Callback interface
    public interface GameCallback {
        void onSuccess(ArrayList<Game> games);
        void onError(Exception e);
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