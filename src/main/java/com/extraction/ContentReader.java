package com.extraction;

import javax.swing.ImageIcon;
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

    private InputStream coverImageInputStream;

    ArrayList<String> getContentsOfRelevantEntriesAsStrings(File epubPublication)
            throws IOException
    {
        final ArrayList<String> MEETINGS_CONTENTS = new ArrayList<>();

            ZipFile epubArchive = new ZipFile(epubPublication);

            for (Enumeration e = epubArchive.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (unnecessaryFile(entry.getName())) continue;

                if (entry.getName().contains("images")) {
                    this.coverImageInputStream = epubArchive.getInputStream(entry);
                    continue;
                }

                String entryContent = new String
                        (getEntryBytes(epubArchive.getInputStream(entry)), StandardCharsets.UTF_8);

                if (!entryContent.contains("treasures") ||
                    !entryContent.contains("ministry")  ||
                    !entryContent.contains("christianLiving")) continue;

                MEETINGS_CONTENTS.add(entryContent);
            }

        return MEETINGS_CONTENTS;
    }

    // `getCoverImage()` must strictly be called after calling `getContentsOfRelevantEntriesAsStrings(...)`
    // because the InputStream to the publication's cover image is initialized in the latter method. If
    // `getCoverImage()` is invoked before `getContentsOfRelevantEntriesAsStrings(...)`, the InputStream
    // (`coverImageInputStream`) will be `null` and cause an NPE exception.
    ImageIcon getCoverImage() throws IOException {
        return new ImageIcon(getEntryBytes(this.coverImageInputStream));
    }

    private boolean unnecessaryFile(String fileName) {
        return
               fileName.contains("mimetype") ||
               fileName.contains("META-INF") ||
               fileName.contains("css")      ||
               !fileName.matches(".*images/mwb_[A-Z]*.*\\.*")  ||
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
}
