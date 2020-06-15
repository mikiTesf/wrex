package com.extraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ContentReader {

    ArrayList<Document> getMeetingFileDOMs(File epubPublication)
            throws IOException
    {
        final ArrayList<String> MEETINGS_CONTENT = new ArrayList<>();

            ZipFile epubArchive = new ZipFile(epubPublication);

            for (Enumeration e = epubArchive.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (unnecessaryFile(entry.getName())) continue;

                String entryContent = new String
                        (getEntryBytes(epubArchive.getInputStream(entry)), StandardCharsets.UTF_8);

                if (!entryContent.contains("treasures") ||
                    !entryContent.contains("ministry")  ||
                    !entryContent.contains("christianLiving")) continue;

                MEETINGS_CONTENT.add(entryContent);
            }

        return parseXHTML(MEETINGS_CONTENT);
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

    private byte[] getEntryBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private ArrayList<Document> parseXHTML(ArrayList<String> meetingsContent) {
        ArrayList<Document> meetingDOMs = new ArrayList<>();

        for (Object meetingContent : meetingsContent) {
            meetingDOMs.add(Jsoup.parse(meetingContent.toString()));
        }

        return meetingDOMs;
    }
}
