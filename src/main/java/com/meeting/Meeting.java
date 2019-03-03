package com.meeting;

public class Meeting {
    private String weekSpan;
    private MeetingSection treasures;
    private MeetingSection improveInMinistry;
    private MeetingSection livingAsChristians;

    public Meeting (
            String weekSpan,
            MeetingSection treasures,
            MeetingSection improveInMinistry,
            MeetingSection livingAsChristians
    ) {
        this.weekSpan = weekSpan;
        this.treasures = treasures;
        this.improveInMinistry = improveInMinistry;
        this.livingAsChristians = livingAsChristians;
    }

    public String getWeekSpan () {
        return this.weekSpan;
    }

    public MeetingSection getTreasures() {
        return this.treasures;
    }

    public MeetingSection getImproveInMinistry() {
        return improveInMinistry;
    }

    public MeetingSection getLivingAsChristians() {
        return livingAsChristians;
    }
}
