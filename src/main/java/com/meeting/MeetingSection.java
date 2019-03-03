package com.meeting;

import java.util.ArrayList;

public class MeetingSection {
    private SectionKind sectionKind;
    private String title;
    // demonstrations, talks, etc...
    private ArrayList<String> parts;

    public MeetingSection (SectionKind sectionKind) {
        this.sectionKind = sectionKind;
        parts = new ArrayList<>();
    }

    public void setSectionTitle(String title) {
        this.title = title;
    }

    public String getSectionTitle() {
        return this.title;
    }

    public SectionKind getSectionKind() {
        return this.sectionKind;
    }

    public ArrayList<String> getParts () {
        return this.parts;
    }

    public void addPart (String part) {
        parts.add(part);
    }
}
