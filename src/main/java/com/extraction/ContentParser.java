package com.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.meeting.MeetingSection;
import com.meeting.Meeting;

import com.meeting.SectionKind;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.meeting.SectionKind.*;

public class ContentParser {
    private ArrayList<Document> meetingExtracts;
    private Element sectionElement;
    private ArrayList<String> meetingContents;
    private final String FILTER_FOR_MINUTE;
    private final Properties ELEMENT_SELECTORS = new Properties();

    public ContentParser(String filterForMinute) {
        this.FILTER_FOR_MINUTE = filterForMinute;
        try {
            this.ELEMENT_SELECTORS.load(getClass().getResourceAsStream("/elementSelectors.properties"));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void parseXHTML() {
        meetingExtracts = new ArrayList<>();
        Object[] meetingContents = this.meetingContents.toArray();

        for (Object meetingContent : meetingContents) {
            meetingExtracts.add(Jsoup.parse(meetingContent.toString()));
        }
    }

    public ArrayList<Meeting> getMeetings() {
        ArrayList<Meeting> meetings = new ArrayList<>();

        for (Document meetingDocument : meetingExtracts) {
            String weekSpan = getWeekSpan(meetingDocument);

            MeetingSection treasures = getMeetingSection
                    (TREASURES, meetingDocument);
            MeetingSection improveInMinistry = getMeetingSection
                    (IMPROVE_IN_MINISTRY, meetingDocument);
            MeetingSection livingAsChristians = getMeetingSection
                    (LIVING_AS_CHRISTIANS, meetingDocument);

            Meeting meeting = new Meeting(weekSpan, treasures, improveInMinistry, livingAsChristians);
            meetings.add(meeting);
        }

        return meetings;
    }

    private String getWeekSpan(Document meetingDoc) {
        sectionElement = meetingDoc.selectFirst(ELEMENT_SELECTORS.getProperty("week.span.selector.element"));
        return sectionElement.text();
    }

    private void setSectionTitle
            (MeetingSection meetingSection, Document meetingDoc) {

        switch (meetingSection.getSECTION_KIND()) {
            case TREASURES:
                sectionElement = meetingDoc.getElementById
                        (ELEMENT_SELECTORS.getProperty("treasures.section.element.id"));
                break;
            case IMPROVE_IN_MINISTRY:
                sectionElement = meetingDoc.getElementById
                        (ELEMENT_SELECTORS.getProperty("improve.in.ministry.section.element.id"));
                break;
            case LIVING_AS_CHRISTIANS:
                sectionElement = meetingDoc.getElementById
                        (ELEMENT_SELECTORS.getProperty("christian.life.section.element.id"));
                break;
        }
        // each section's title is within the first "h2" element under the corresponding
        // div with `id` "section<sectionNumber>"
        meetingSection.setSectionTitle(sectionElement.selectFirst
                (ELEMENT_SELECTORS.getProperty("meeting.section.title.selector.element")).text());
    }

    private MeetingSection getMeetingSection
            (SectionKind sectionKind, Document meetingDocument) {

        MeetingSection meetingSection = new MeetingSection(sectionKind);

        setSectionTitle(meetingSection, meetingDocument);

        Elements presentations = new Elements();
        sectionElement.selectFirst(ELEMENT_SELECTORS.getProperty("presentations.group.selector.element"));
        presentations.addAll(sectionElement.getElementsByTag(ELEMENT_SELECTORS.getProperty("presentation.selector.element")));

        if (meetingSection.getSECTION_KIND() == LIVING_AS_CHRISTIANS) {
            presentations.remove(0); // transition song element
            presentations.remove(presentations.size() - 1); // concluding song and prayer element
            presentations.remove(presentations.size() - 1); // next week preview element
        }

        String topic;
        for (Element listItem : presentations) {
            topic = listItem.selectFirst
                    (ELEMENT_SELECTORS.getProperty("presentation.titles.selector.element")).text();
            if (!topic.contains(FILTER_FOR_MINUTE)) continue;

            topic = topic.substring(0, topic.indexOf(FILTER_FOR_MINUTE)) + FILTER_FOR_MINUTE + ")";
            meetingSection.addPart(topic);
        }

        return meetingSection;
    }

    public void setMeetingContents(ArrayList<String> meetingContents) {
        this.meetingContents = meetingContents;
        this.parseXHTML();
    }
}
