package com.extraction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EPUBContentExtractor {

    public ArrayList<ArrayList<String>> getContentsOfRelevantEntriesAsStrings
            (File[] epubPublications) throws IOException {

        final ArrayList<ArrayList<String>> ALL_MEETINGS_CONTENTS = new ArrayList<>();

        for (File publication : epubPublications) {
            ArrayList<String> publicationExtracts = new ArrayList<>();
            ZipFile epubArchive = new ZipFile(publication);

            for (Enumeration e = epubArchive.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (unnecessaryFile(entry.getName())) continue;

                String entryContent = getEntryAsString(epubArchive.getInputStream(entry));

                if (!entryContent.contains("treasures") ||
                    !entryContent.contains("ministry")  ||
                    !entryContent.contains("christianLiving")) continue;

                publicationExtracts.add(entryContent);
            }

            ALL_MEETINGS_CONTENTS.add(publicationExtracts);
        }

        return ALL_MEETINGS_CONTENTS;
    }

    private boolean unnecessaryFile(String fileName) {
        return
               fileName.contains("mimetype")    ||
               fileName.contains("META-INF")    ||
               fileName.contains("css")         ||
               fileName.contains("images")      ||
               fileName.contains("extracted")   ||
               // different numbers appear after "pagenav"
               fileName.contains("pagenav")     ||
               fileName.contains("content.opf") ||
               fileName.contains("cover.xhtml") ||
               // both "toc.ncx" and "toc.xhtml"
               fileName.contains("toc.");
    }

    private String getEntryAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
