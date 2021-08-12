package com.extraction;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class ContentReaderTest {

    private final File HEALTHY_MWB = new File(getClass().getResource("/mwb_E_202012.epub").getFile());
    private final File TEXT_FILE_WITH_EPUB_EXTENSION = new File(getClass().getResource("/mwb_XX_0000.epub").getFile());
    private final ContentReader CONTENT_READER = new ContentReader();

    @Test
    @DisplayName("Should finish without throwing an IOException")
    void shouldCompleteWithoutAnException() {
        assertDoesNotThrow(() -> CONTENT_READER.getMeetingFileDOMs(HEALTHY_MWB));
    }

    @Test
    @DisplayName("Should throw an IOException for the non-mwb file provided")
    void shouldThrowAnIOException() {
        assertThrows(IOException.class,
                () -> CONTENT_READER.getMeetingFileDOMs(TEXT_FILE_WITH_EPUB_EXTENSION));
    }

    @Test
    @DisplayName("Should extract 4 meeting DOM objects")
    void shouldExtract4MeetingDOMs() {
        ArrayList<Document> meetingDOMs = new ArrayList<>();
        // `healthyWorkbook` has 4 meetings
        final int MEETING_DOM_COUNT = 4;

        try {
            meetingDOMs = CONTENT_READER.getMeetingFileDOMs(HEALTHY_MWB);
        } catch (IOException e) {
            fail();
        }

        assertEquals(MEETING_DOM_COUNT, meetingDOMs.size());
    }
}
