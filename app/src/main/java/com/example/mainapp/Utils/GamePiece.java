package com.example.mainapp.Utils;

public enum GamePiece {
    L1(2, 3),
    L2(3,4),
    L3(4,6),
    L4(5,7),
    NET(4, 4),
    PROCESSOR(6, 6);

    private int teleopPoints;
    private int autoPoints;

    GamePiece(int autoPoints, int teleopPoints){
        this.autoPoints = autoPoints;
        this.teleopPoints = teleopPoints;
    }

    public int getTeleopPoints(){return  this.teleopPoints;}

    public int getAutoPoints(){return  this.autoPoints;}
}
