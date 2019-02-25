package com.meeting;

public class Treasures extends Meeting {
    // There is only one part I am interested in under the "Treasures" section
    private String title;

    public Treasures (String title) {
        super(Meeting.TREASURES);
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
