package com.example.mainapp.Utils;

/**
 * GamePiece enum represents the different scoring objectives available in the competition.
 * Each piece (L1-L4, Net, Processor) has distinct point values for Autonomous and Teleoperated periods.
 */
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

/**
 * Executes the logic associated with the getTeleopPoints operation.
 * @return the value produced by this method.
 */
    public int getTeleopPoints(){return  this.teleopPoints;}

/**
 * Executes the logic associated with the getAutoPoints operation.
 * @return the value produced by this method.
 */
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