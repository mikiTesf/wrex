package com.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Extractor {

    private final ContentReader CONTENT_READER;
    private final ContentParser CONTENT_PARSER;

    public Extractor(String filterForMinute) {
        this.CONTENT_READER = new ContentReader();
        this.CONTENT_PARSER = new ContentParser(filterForMinute);
    }

    public ArrayList<PubExtract> getPublicationExtracts(File[] publicationFiles)
        throws IOException
    {
        ArrayList<PubExtract> publicationsExtracts = new ArrayList<>();

        for (File epubFile : publicationFiles) {
            PubExtract pubExtract = new PubExtract();
            CONTENT_PARSER.setMeetingContents(CONTENT_READER.getContentsOfRelevantEntriesAsStrings(epubFile));

            pubExtract.setPublicationName(
                    epubFile.getName().replaceAll("\\.[e|E][p|P][u|U][b|B]", ""));
            pubExtract.setPublicationImage(CONTENT_READER.getCoverImage());
            pubExtract.setMeetings(CONTENT_PARSER.getMeetings());
            publicationsExtracts.add(pubExtract);
        }

        return publicationsExtracts;
    }
}
