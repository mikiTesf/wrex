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
        Treasures treasures;
        ImproveInMinistry improveInMinistry;
        LivingAsChristians livingAsChristians;
        treasureDoc = Jsoup.parse(XHTMLFile, "UTF-8");
        /* *** Treasures ***/
        treasures = getTreasures();
        /* *** Improve in your ministry ***/
        improveInMinistry = getMinistryImprovements();
        /* *** Christian Living ***/
        livingAsChristians = getLivingAsChristians();
        ArrayList<Meeting> meetings = new ArrayList<>();
        meetings.add(treasures);
        meetings.add(improveInMinistry);
        meetings.add(livingAsChristians);
        return meetings;
    }

    public String getWeekSpan () {
        treasureElement = treasureDoc.selectFirst("title");
        return treasureElement.text();
    }

    private Treasures getTreasures() {
        treasureElement = treasureDoc.getElementById("section2").selectFirst("p");
        return new Treasures(treasureElement.text());
    }

    private ImproveInMinistry getMinistryImprovements() {
        ImproveInMinistry improveInMinistry = new ImproveInMinistry();
        String title;
        treasureElement = treasureDoc.getElementById("section3").selectFirst("ul");
        Elements presentations = treasureElement.getElementsByTag("li");

        for (Element listItem : presentations) {
            title = listItem.selectFirst("p").text();
            // ደቂቃ
            title = title.substring(0, title.indexOf("ቂቃ")) + "ቂቃ)";
            improveInMinistry.addPart(title);
        }
        return improveInMinistry;
    }

    private LivingAsChristians getLivingAsChristians() {
        LivingAsChristians livingAsChristians = new LivingAsChristians();
        String title;
        treasureElement = treasureDoc.getElementById("section4").selectFirst("ul");
        Elements christianMeetings = treasureElement.getElementsByTag("li");
        // The paragraphs inside the first (index -> 0) and the last two list items should not
        // be included in the schedule. Therefore their indexes must be calculated in advance
        for (Element listItem : christianMeetings) {
            if (
                // first element is a transition song
                christianMeetings.indexOf(listItem) == 0 ||
                // the last two elements are preview and concluding song respectively
                christianMeetings.indexOf(listItem) == christianMeetings.size() - 1 ||
                christianMeetings.indexOf(listItem) == christianMeetings.size() - 2
            )
                continue;
            title = listItem.selectFirst("p").text();
            title = title.substring(0, title.indexOf("ቂቃ)")) + "ቂቃ)";
            livingAsChristians.addPart(title);
        }
        return livingAsChristians;
    }
}
