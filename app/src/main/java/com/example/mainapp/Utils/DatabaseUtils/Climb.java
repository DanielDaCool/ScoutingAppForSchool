package com.example.mainapp.Utils.DatabaseUtils;

public enum Climb {
    HIGH(12),
    LOW(6),
    FAILED(0),
    DIDNT_TRY(0);
    int points;
    Climb(int points){
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

}
