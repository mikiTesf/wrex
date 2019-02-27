package com.meeting;

import java.util.ArrayList;

public abstract class MeetingSection {
    private String kind;
    public static final String TREASURES            = "TREASURES";
    public static final String IMPROVE_IN_MINISTRY  = "IMPROVE_IN_MINISTRY";
    public static final String LIVING_AS_CHRISTIANS = "LIVING_AS_CHRISTIANS";
    // demonstrations, talks, etc...
    private ArrayList<String> parts;

    MeetingSection (String kind) {
        this.kind = kind;
        parts = new ArrayList<>();
    }

    public String getKind () {
        return this.kind;
    }

    public ArrayList<String> getParts () {
        return this.parts;
    }

    public void addPart (String part) {
        parts.add(part);
    }
}
