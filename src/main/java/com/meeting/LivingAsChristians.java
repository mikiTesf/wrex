package com.meeting;

import java.util.ArrayList;

public class LivingAsChristians extends Meeting {
    private ArrayList<String> parts;

    public LivingAsChristians() {
        super(Meeting.LIVING_AS_CHRISTIANS);
        parts = new ArrayList<>();
    }

    public ArrayList<String> getParts () {
        return this.parts;
    }

    public void addPart (String part) {
        parts.add(part);
    }
}
