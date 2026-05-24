package com.example.mainapp.TBAHelpers;

/**
 * EVENTS enum defines the supported FIRST Robotics Competition (FRC) events in Israel for the 2025 season.
 * Each enum constant maps to a specific event key used by The Blue Alliance API.
 */
public enum EVENTS {

    DISTRICT_1("2025isde1"),
    DISTRICT_2("2025isde2"),

    DISTRICT_3("2025isde3"),
    DISTRICT_4("2025isde4"),
    DCMP("2025iscmp");
    private String eventKey;
    EVENTS(String eventKey){
        this.eventKey = eventKey;
    }
/**
 * Executes the logic associated with the getEventKey operation.
 * @return the value produced by this method.
 */
    public String getEventKey(){return this.eventKey;}


}