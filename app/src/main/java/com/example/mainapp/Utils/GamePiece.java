package com.example.mainapp.Utils;

public enum GamePiece {
    L1(2, 3),
    L2(3,4),
    L3(4,6),
    L4(5,7),;

    private int teleopPoints;
    private int autoPoints;

    GamePiece(int teleopPoitns, int teleopPoints){
        this.autoPoints = autoPoints;
        this.teleopPoints = teleopPoints;
    }

    public int getTeleopPoitns(){return  this.teleopPoints;}

    public int getAutoPoints(){return  this.autoPoints;}
}
