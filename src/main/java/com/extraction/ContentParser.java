package com.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    private File publicationFolder;

    public ContentParser() {}

    public void readRawHTML() {
        meetingExtracts = new ArrayList<>();
        try {
            File[] XHTMLFiles = publicationFolder.listFiles();
            /*
            * Sorting the 'XHTMLFiles' array is important because `listFiles()` does not guarantee
            * order through neither path nor name (both would have worked in this case). This in
            * turn affects the order of the schedule in the Excel file that gets generated next
            * */
            // noinspection ConstantConditions
            Arrays.sort(XHTMLFiles);
            for (File XHTMLFile : XHTMLFiles) {
                meetingExtracts.add(Jsoup.parse(XHTMLFile, "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        sectionElement = meetingDoc.selectFirst("title");
        return sectionElement.text();
    }

    private void setSectionTitle
            (MeetingSection meetingSection, Document meetingDoc) {

        switch (meetingSection.getSectionKind()) {
            case TREASURES:
                sectionElement = meetingDoc.getElementById("section2");
                break;
            case IMPROVE_IN_MINISTRY:
                sectionElement = meetingDoc.getElementById("section3");
                break;
            case LIVING_AS_CHRISTIANS:
                sectionElement = meetingDoc.getElementById("section4");
                break;
        }
        // each section's title is within the first "h2" element under the corresponding
        // div with `id` "section<sectionNumber>"
        meetingSection.setSectionTitle(sectionElement.selectFirst("h2").text());
    }

    private MeetingSection getMeetingSection
            (SectionKind sectionKind, Document meetingDocument) {

        final String FILTER_TEXT = " ደቂቃ";
        MeetingSection meetingSection = new MeetingSection(sectionKind);

        setSectionTitle(meetingSection, meetingDocument);

        Elements presentations = new Elements();
        sectionElement.selectFirst("ul");
        presentations.addAll(sectionElement.getElementsByTag("li"));

        if (meetingSection.getSectionKind() == LIVING_AS_CHRISTIANS) {
            presentations.remove(0); // transition song element
            presentations.remove(presentations.size() - 1); // concluding song and prayer element
            presentations.remove(presentations.size() - 1); // next week preview element
        }

        String topic;
        for (Element listItem : presentations) {
            topic = listItem.selectFirst("p").text();
            if (!topic.contains(FILTER_TEXT)) continue;

            topic = topic.substring(0, topic.indexOf(FILTER_TEXT)) + FILTER_TEXT + ")";
            meetingSection.addPart(topic);
        }

        return meetingSection;
    }

    public void setPublicationFolder(File publicationFolder) {
        this.publicationFolder = publicationFolder;
    }
}
