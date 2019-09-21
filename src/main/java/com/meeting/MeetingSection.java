package com.meeting;

import java.util.ArrayList;

public class MeetingSection {
    private final SectionKind SECTION_KIND;
    private String title;
    // demonstrations, talks, etc...
    private final ArrayList<String> PARTS;

    public MeetingSection(SectionKind SECTION_KIND) {
        this.SECTION_KIND = SECTION_KIND;
        PARTS = new ArrayList<>();
    }

    public SectionKind getSECTION_KIND() {
        return this.SECTION_KIND;
    }

    public void setSectionTitle(String title) {
        this.title = title;
    }

    public String getSectionTitle() {
        return this.title;
    }

    public ArrayList<String> getParts () {
        return this.PARTS;
    }

    public void addPart (String part) {
        PARTS.add(part);
    }
}
