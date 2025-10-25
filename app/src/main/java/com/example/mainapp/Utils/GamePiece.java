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
    public static GamePiece getGamePieceFromString(String gamePiece){
        switch (gamePiece.toLowerCase()){
            case "l1":
                return L1;
            case "l2":
                return L2;

            case "l3":
                return L3;
            case "l4":
                return L4;
            case "net":
                return NET;
            case "processor":
                return PROCESSOR;


        }
        return L1;
    }
}
