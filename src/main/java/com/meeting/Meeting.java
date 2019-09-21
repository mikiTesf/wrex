package com.meeting;

public class Meeting {
    private final String WEEK_SPAN;
    private final MeetingSection TREASURES;
    private final MeetingSection IMPROVE_IN_MINISTRY;
    private final MeetingSection LIVING_AS_CHRISTIANS;

    public Meeting (
            String WEEK_SPAN,
            MeetingSection TREASURES,
            MeetingSection IMPROVE_IN_MINISTRY,
            MeetingSection LIVING_AS_CHRISTIANS
    ) {
        this.WEEK_SPAN = WEEK_SPAN.replaceAll("[-|–]", " – ");
        this.TREASURES = TREASURES;
        this.IMPROVE_IN_MINISTRY = IMPROVE_IN_MINISTRY;
        this.LIVING_AS_CHRISTIANS = LIVING_AS_CHRISTIANS;
    }

    public String getWEEK_SPAN() {
        return WEEK_SPAN;
    }

    public MeetingSection getTREASURES() {
        return TREASURES;
    }

    public MeetingSection getIMPROVE_IN_MINISTRY() {
        return IMPROVE_IN_MINISTRY;
    }

    public MeetingSection getLIVING_AS_CHRISTIANS() {
        return LIVING_AS_CHRISTIANS;
    }
}
