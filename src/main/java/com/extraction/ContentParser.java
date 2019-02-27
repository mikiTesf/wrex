package com.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    private Element treasureElement;

    public ContentParser(File publicationFolder) {
        meetingExtracts = new ArrayList<>();
        try {
            //noinspection ConstantConditions
            for (File XHTMLFile : publicationFolder.listFiles()) {
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
        treasureElement = meetingDoc.selectFirst("title");
        return treasureElement.text();
    }

    private Treasures getTreasures(Document meetingDoc) {
        treasureElement = meetingDoc.getElementById("section2").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");

        return (Treasures) addPartsToMeeting(presentations, new Treasures());
    }

    private ImproveInMinistry getMinistryImprovements(Document meetingDoc) {
        treasureElement = meetingDoc.getElementById("section3").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");

        return (ImproveInMinistry) addPartsToMeeting(presentations, new ImproveInMinistry());
    }

    private LivingAsChristians getLivingAsChristians(Document meetingDoc) {
        treasureElement = meetingDoc.getElementById("section4").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");
        // The paragraphs inside the first (index -> 0) and the last two list items should not
        // be included in the schedule. They must be removed in advance
        presentations.remove(0); // transition song element
        presentations.remove(presentations.size() - 1); // concluding song and prayer element
        // the ArrayList re-sizes on the previous `remove` so the index of
        // the last element must be found with the same expression:
        // `presentations.size() - 1`
        presentations.remove(presentations.size() - 1); // next week preview element

        return (LivingAsChristians) addPartsToMeeting(presentations, new LivingAsChristians());
    }

    private MeetingSection addPartsToMeeting(Elements presentations, MeetingSection meetingSection) {
        String title;
        for (Element listItem : presentations) {
            title = listItem.selectFirst("p").text();
            // filter for ደቂቃ
            if (!title.contains("ደቂቃ")) continue;
            title = title.substring(0, title.indexOf(" ደቂቃ")) + " ደቂቃ)";
            meetingSection.addPart(title);
        }

        return meetingSection;
    }
}
