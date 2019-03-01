package com.meeting;

public class Meeting {
    private String weekSpan;
    private Treasures treasures;
    private ImproveInMinistry improveInMinistry;
    private LivingAsChristians livingAsChristians;

    public Meeting (
            String weekSpan,
            Treasures treasures,
            ImproveInMinistry improveInMinistry,
            LivingAsChristians livingAsChristians
    ) {
        this.weekSpan = weekSpan;
        this.treasures = treasures;
        this.improveInMinistry = improveInMinistry;
        this.livingAsChristians = livingAsChristians;
    }

    public String getWeekSpan () {
        return this.weekSpan;
    }

    public Treasures getTreasures() {
        return this.treasures;
    }

    public ImproveInMinistry getImproveInMinistry() {
        return improveInMinistry;
    }

    public LivingAsChristians getLivingAsChristians() {
        return livingAsChristians;
    }
}
