package com.example.mainapp.TBAHelpers;

/**
 * Represents the EVENTS component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
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