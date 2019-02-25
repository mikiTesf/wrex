package com.meeting;

import java.util.ArrayList;

public class ImproveInMinistry extends Meeting {
    private ArrayList<String> demonstrations;

    public ImproveInMinistry() {
        super(Meeting.IMPROVE_IN_MINISTRY);
        demonstrations = new ArrayList<>();
    }

    public ArrayList<String> getDemonstrations () {
        return this.demonstrations;
    }

    public void addPart(String part) {
        demonstrations.add(part);
    }
}
