package com.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.meeting.MeetingSection;
import com.meeting.ImproveInMinistry;
import com.meeting.LivingAsChristians;
import com.meeting.Treasures;
import com.meeting.Meeting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            * order neither through path or name (both would have worked in this case). This in
            * turn affects the order of the schedule in the Excel file that gets generated next
            * */
            //noinspection ConstantConditions
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
            Treasures treasures = getTreasures(meetingDocument);
            ImproveInMinistry improveInMinistry = getMinistryImprovements(meetingDocument);
            LivingAsChristians livingAsChristians = getLivingAsChristians(meetingDocument);

            Meeting meeting = new Meeting(weekSpan, treasures, improveInMinistry, livingAsChristians);
            meetings.add(meeting);
        }

        return meetings;
    }

    private String getWeekSpan(Document meetingDoc) {
        sectionElement = meetingDoc.selectFirst("title");
        return sectionElement.text();
    }

    private Treasures getTreasures(Document meetingDoc) {
        Elements presentations = new Elements();
        sectionElement = meetingDoc.getElementById("section2");
        // the section's topic is in the first "h2" element under the corresponding "section*"
        presentations.add(sectionElement.selectFirst("h2"));
        sectionElement.selectFirst("ul");
        presentations.addAll(sectionElement.getElementsByTag("li"));

        return (Treasures) addTitleAndPartsToSection(presentations, new Treasures());
    }

    private ImproveInMinistry getMinistryImprovements(Document meetingDoc) {
        Elements presentations = new Elements();
        sectionElement = meetingDoc.getElementById("section3");
        presentations.add(sectionElement.selectFirst("h2"));
        sectionElement.selectFirst("ul");
        presentations.addAll(sectionElement.getElementsByTag("li"));

        return (ImproveInMinistry) addTitleAndPartsToSection(presentations, new ImproveInMinistry());
    }

    private LivingAsChristians getLivingAsChristians(Document meetingDoc) {
        Elements presentations = new Elements();
        sectionElement = meetingDoc.getElementById("section4");
        presentations.add(sectionElement.selectFirst("h2"));
        sectionElement.selectFirst("ul");
        presentations.addAll(sectionElement.getElementsByTag("li"));
        // The paragraphs inside the 2nd (index -> 1) and the last two list items should not
        // be included in the schedule. They must be removed before passing them to the method
        // that adds the presentations to this MeetingSection
        presentations.remove(1); // transition song element
        presentations.remove(presentations.size() - 1); // concluding song and prayer element
        // the ArrayList re-sizes on the previous `remove` so the index of
        // the last element can only be calculated with the same expression:
        // `presentations.size() - 1`
        presentations.remove(presentations.size() - 1); // next week preview element

        return (LivingAsChristians) addTitleAndPartsToSection(presentations, new LivingAsChristians());
    }

    private MeetingSection addTitleAndPartsToSection(Elements presentations, MeetingSection meetingSection) {
        String topic;
        meetingSection.setSectionTitle(presentations.get(0).text());
        // not to add an exception in the next loop, the
        // first element (an "h2") is better removed
        presentations.remove(0);
        for (Element listItem : presentations) {
            topic = listItem.selectFirst("p").text();
            // filter for ደቂቃ
            if (!topic.contains(" ደቂቃ")) continue;
            topic = topic.substring(0, topic.indexOf(" ደቂቃ")) + " ደቂቃ)";
            meetingSection.addPart(topic);
        }

        return meetingSection;
    }

    public void setPublicationFolder(File publicationFolder) {
        this.publicationFolder = publicationFolder;
    }
}
