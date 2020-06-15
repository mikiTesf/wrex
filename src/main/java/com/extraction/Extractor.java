package com.extraction;

import java.io.File;
import java.io.IOException;

public class Extractor {

    private final ContentReader CONTENT_READER;
    private final ContentParser CONTENT_PARSER;

    public Extractor(String filterForMinute) throws IOException {
        this.CONTENT_READER = new ContentReader();
        this.CONTENT_PARSER = new ContentParser(filterForMinute);
    }

    public PubExtract getPublicationExtracts(File pubFile)
        throws IOException, IllegalStateException
    {
        PubExtract pubExtract = new PubExtract();
        CONTENT_PARSER.setMeetingContents(CONTENT_READER.getMeetingFileDOMs(pubFile));
        pubExtract.setPublicationName(
                pubFile.getName().replaceAll("\\.[eE][pP][uU][bB]", ""));
        pubExtract.setMeetings(CONTENT_PARSER.getMeetings());

        return pubExtract;
    }
}
