package com.meeting;

public abstract class Meeting {
    private String kind;
    public static final String TREASURES = "TREASURES";
    public static final String IMPROVE_IN_MINISTRY = "IMPROVE_IN_MINISTRY";
    public static final String LIVING_AS_CHRISTIANS = "LIVING_AS_CHRISTIANS";

    Meeting (String kind) {
        this.kind = kind;
    }

    public String getKind () {
        return this.kind;
    }
}
