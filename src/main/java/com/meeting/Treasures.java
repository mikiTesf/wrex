package com.meeting;

public class Treasures extends Meeting {
    public Treasures () {
        super(Meeting.TREASURES);
    }

    public String get10MinuteTalk () {
        return this.parts.get(0);
    }

    public String getDiggingForGems () {
        return this.parts.get(1);
    }

    public String getBibleReading () {
        return this.parts.get(2);
    }
}
