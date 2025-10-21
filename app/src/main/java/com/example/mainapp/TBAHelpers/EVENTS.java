package com.example.mainapp.TBAHelpers;

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

    @Override
    public String toString() {
        return eventKey;
    }

}
