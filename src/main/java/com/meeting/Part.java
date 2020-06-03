package com.meeting;

public class Part {
    private String partTitle;
    private String presenterName;

    public Part(String partTitle, String presenterName) {
        this.partTitle = partTitle;
        this.presenterName = presenterName;
    }

    public String getPartTitle() {
        return partTitle;
    }

    public void setPartTitle(String partTitle) {
        this.partTitle = partTitle;
    }

    public String getPresenterName() {
        return presenterName;
    }

    public void setPresenterName(String presenterName) {
        this.presenterName = presenterName;
    }
}
