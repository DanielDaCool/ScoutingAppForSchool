package com.example.mainapp.Utils;

public enum GamePiece {
    L1(3, 2),
    L2(4,3),
    L3(6,4),
    L4(7,5),
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
