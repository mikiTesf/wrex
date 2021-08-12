package com.extraction;

import com.meeting.Meeting;
import com.meeting.MeetingSection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


class ContentParserTest {

    private final File HEALTHY_MWB = new File(getClass().getResource("/mwb_E_202012.epub").getFile());
    private final File TEXT_FILE_WITH_EPUB_EXTENSION = new File(getClass().getResource("/mwb_XX_0000.epub").getFile());
    private final File MWB_WITH_MEETINGS_STRIPPED = new File(getClass().getResource("/mwb_E_111111.epub").getFile());
    private final ContentReader CONTENT_READER = new ContentReader();

    /*
     * The failure of this test can be interpreted in either one of 2 ways. One is that the workbook did not come with
     * any meeting DOMs. Or two, the internal structure of the workbook has changed and the DOM selectors (ids and classes)
     * used by WREX to extract the meetings are outdated and so it failed to extract the meeting's details.*/
    @Test
    @DisplayName("Should throw an IllegalStateException when the supplied workbook doesn't contain meeting XHTML files")
    void shouldThrowAnIllegalStateException() throws IOException {
        final ContentParser contentParser = new ContentParser(" min.");
        assertThrows(IllegalStateException.class, () -> contentParser.setMeetingContents(
                CONTENT_READER.getMeetingFileDOMs(MWB_WITH_MEETINGS_STRIPPED)
        ));
    }

    @Test
    @DisplayName("Should throw an IOException when the supplied file is not a meeting workbook")
    void shouldThrowAnIOException() throws IOException {
        final ContentParser contentParser = new ContentParser(" min.");
        assertThrows(IOException.class, () -> contentParser.setMeetingContents(
                CONTENT_READER.getMeetingFileDOMs(TEXT_FILE_WITH_EPUB_EXTENSION)
        ));
    }

    @Test
    @DisplayName("Should return an ArrayList of size 4 containing `Meeting` objects")
    void shouldReturn4MeetingObjects() throws IOException {
        final ContentParser contentParser = new ContentParser(" min.");
        final int MEETING_COUNT = 4;

        contentParser.setMeetingContents(CONTENT_READER.getMeetingFileDOMs(HEALTHY_MWB));
        ArrayList<Meeting> meetings = contentParser.getMeetings();

        assertEquals(MEETING_COUNT, meetings.size());
    }

    @Test
    @DisplayName("Each meeting extracted from the workbook should have the corresponding parts")
    void eachMeetingShouldHaveTheAppropriateNumberOfParts() throws IOException {
        final ContentParser contentParser = new ContentParser(" min.");

        contentParser.setMeetingContents(CONTENT_READER.getMeetingFileDOMs(HEALTHY_MWB));
        ArrayList<Meeting> meetings = contentParser.getMeetings();

        // First Meeting
        Meeting meeting1 = meetings.get(0);
        assertEquals("December 7 – 13", meeting1.getWEEK_SPAN());

        MeetingSection treasures = meeting1.getTREASURES();
        MeetingSection improveInMinistry = meeting1.getIMPROVE_IN_MINISTRY();
        MeetingSection christianLife = meeting1.getLIVING_AS_CHRISTIANS();

        // Check if they contain the correct number of parts
        assertEquals(3, treasures.getParts().size());
        assertEquals(3, improveInMinistry.getParts().size());
        assertEquals(2, christianLife.getParts().size());
        // Check the parts' contents
        // Treasures
        assertEquals("“Love for Jehovah Stronger Than Love for Family”: (10 min.)", treasures.getParts().get(0).getPartTitle());
        assertEquals("Digging for Spiritual Gems: (10 min.)", treasures.getParts().get(1).getPartTitle());
        assertEquals("Bible Reading: (4 min.)", treasures.getParts().get(2).getPartTitle());
        // Improve In Ministry
        assertEquals("Initial Call Video: (4 min.)", improveInMinistry.getParts().get(0).getPartTitle());
        assertEquals("Initial Call: (4 min.)", improveInMinistry.getParts().get(1).getPartTitle());
        assertEquals("Talk: (5 min.)", improveInMinistry.getParts().get(2).getPartTitle());
        // Christian Life
        assertEquals("“We Show Love by Supporting Jehovah’s Discipline”: (15 min.)", christianLife.getParts().get(0).getPartTitle());
        assertEquals("Congregation Bible Study: (30 min.)", christianLife.getParts().get(1).getPartTitle());

        // Check for long week-spans
        assertEquals("December 28, 2020 – January 3, 2021", meetings.get(3).getWEEK_SPAN());
    }
}
