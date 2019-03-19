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
        this.weekSpan = weekSpan.replaceAll("[-|–]", " – ");
        this.treasures = treasures;
        this.improveInMinistry = improveInMinistry;
        this.livingAsChristians = livingAsChristians;
    }

    public String getWeekSpan () {
        return weekSpan;
    }

    public MeetingSection getTreasures() {
        return treasures;
    }

    public MeetingSection getImproveInMinistry() {
        return improveInMinistry;
    }

    public MeetingSection getLivingAsChristians() {
        return livingAsChristians;
    }
}
