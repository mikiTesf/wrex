import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.meeting.ImproveInMinistry;
import com.meeting.LivingAsChristians;
import com.meeting.Meeting;
import com.meeting.Treasures;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class ContentParser {
    private Document treasureDoc;
    private Element treasureElement;

    ContentParser() {}

    ArrayList<Meeting> parse(File XHTMLFile) throws IOException {
        Meeting treasures;
        Meeting improveInMinistry;
        Meeting livingAsChristians;
        treasureDoc = Jsoup.parse(XHTMLFile, "UTF-8");
        /* *** Treasures ***/
        treasures = getTreasures();
        /* *** Improve in your ministry ***/
        improveInMinistry = getMinistryImprovements();
        /* *** Living as Christians ***/
        livingAsChristians = getLivingAsChristians();

        ArrayList<Meeting> meetings = new ArrayList<>();
        meetings.add(treasures);
        meetings.add(improveInMinistry);
        meetings.add(livingAsChristians);

        return meetings;
    }

    String getWeekSpan () {
        treasureElement = treasureDoc.selectFirst("title");
        return treasureElement.text();
    }

    private Treasures getTreasures() {
        treasureElement = treasureDoc.getElementById("section2").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");

        return (Treasures) addPartsToMeeting(presentations, new Treasures());
    }

    private ImproveInMinistry getMinistryImprovements() {
        treasureElement = treasureDoc.getElementById("section3").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");

        return (ImproveInMinistry) addPartsToMeeting(presentations, new ImproveInMinistry());
    }

    private LivingAsChristians getLivingAsChristians() {
        treasureElement = treasureDoc.getElementById("section4").selectFirst("ul");
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

    private Meeting addPartsToMeeting(Elements presentations, Meeting meeting) {
        String title;
        for (Element listItem : presentations) {
            title = listItem.selectFirst("p").text();
            // filter for ደቂቃ
            if (!title.contains("ደቂቃ")) continue;
            title = title.substring(0, title.indexOf("ደቂቃ")) + "ደቂቃ)";
            meeting.addPart(title);
        }

        return meeting;
    }
}
