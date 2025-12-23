package com.example.mainapp.TBAHelpers;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    public List<Team> getEventTeams(EVENTS eventKey, TeamCallback callback) throws IOException, JSONException {
        ArrayList<Team> teams = new ArrayList<Team>();
        new Thread(() -> {
            try {
                String json = fetchData("/event/" + eventKey.getEventKey() + "/teams/simple");
                System.out.println("JSON Response length: " + json.length());

                JSONArray jsonArray = new JSONArray(json);



                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject teamJson = jsonArray.getJSONObject(i);
                    teams.add(JsonParser.parseToTeam(teamJson));
                }

               teams.sort((t1, t2) -> Integer.compare(t1.getTeamNumber(), t2.getTeamNumber()));

                callback.onSuccess(teams);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();



        return teams;
    }
    public void getGamesCount(EVENTS eventKey, CountCallback callback){
        new Thread(()->{
            try{
                String json = fetchData("/event/" + eventKey + "/matches");
                System.out.println("JSON Response length: " + json.length());

                callback.onSuccess(new JSONArray(json).length());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();


    }
    public void getEventGames(EVENTS eventKey, GameCallback callback) {
        new Thread(() -> {
            try {
                String json = fetchData("/event/" + eventKey.getEventKey() + "/matches");
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


    public void getTeam(int teamNumber, SingleTeamCallback callback) throws IOException, JSONException {
        new Thread(()->{
            try{
                String json = fetchData("/team/frc" + teamNumber);
                callback.onSuccess(JsonParser.parseToTeam(new JSONObject(json)));
            }
            catch (Exception e){
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }

    public void getIsraeliTeams(TeamListCallback callback) {
        new Thread(()->{
            Set<Team> israeliTeams = new HashSet<>();

            // Get teams from each Israeli district event
            EVENTS[] israelEvents = {
                    EVENTS.DISTRICT_1,
                    EVENTS.DISTRICT_2,
                    EVENTS.DISTRICT_3,
                    EVENTS.DISTRICT_4,
                    EVENTS.DCMP
            };

            for (EVENTS event : israelEvents) {
                try {
                    String json = fetchData("/event/" + event.getEventKey() + "/teams");
                    JSONArray jsonArray = new JSONArray(json);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject teamJson = jsonArray.getJSONObject(i);
                        Team team = JsonParser.parseToTeam(teamJson);
                        israeliTeams.add(team);  // Set automatically handles duplicates
                    }
                } catch (Exception e) {
                    Log.w("TBAApiManager", "Could not fetch teams from " + event.getEventKey());
                }
            }

            ArrayList<Team> finalTeams = new ArrayList<>(israeliTeams);
            callback.onSuccess(finalTeams);

        }).start();
    }

    public interface TeamListCallback{
        void onSuccess(ArrayList<Team> teams);

    }
    // Callback interface
    public interface GameCallback {
        void onSuccess(ArrayList<Game> games);
        void onError(Exception e);
    }

    public interface TeamCallback{
        void onSuccess(ArrayList<Team> teams);
        void onError(Exception e);
    }
    public interface SingleTeamCallback{
        void onSuccess(Team t);
        void onError(Exception e);
    }
    public interface  CountCallback{
        void onSuccess(int count);
        void onError(Exception e);
    }
}