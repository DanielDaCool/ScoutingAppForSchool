package com.example.mainapp.Utils;

public enum CLIMB {
    HIGH(12),
    LOW(6),
    FAILED(0),
    DIDNT_TRY(0);
    int points;
    CLIMB(int points){
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public static CLIMB convertFromString(String str){
        switch (str.toUpperCase()){
            case "HIGH" ->{
                return HIGH;
            }
            case "LOW" ->{
                return LOW;
            }
            case "FAILED" -> {
                return FAILED;
            }
        }
        return  DIDNT_TRY;
    }
    public static String convertToString(CLIMB c){
        switch (c){
            case HIGH -> {
                return "HIGH";
            }
            case LOW -> {
                return "LOW";
            }
            case FAILED -> {
                return "FAILED";
            }
            case DIDNT_TRY -> {
                return "DIDNT_TRY";
            }
        }
        return "";
    }
}
