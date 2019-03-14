package com.extraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EPUBContentExtractor {

    public void unzip(File[] EPUBFiles, Charset charset) throws IOException {
        final File destination = new File(".content/");
        String OEBPSFolderPath = "";

        for (File EPUBFile : EPUBFiles) {
            try (ZipInputStream zipIn = new ZipInputStream
                    (new FileInputStream(EPUBFile), charset)) {
                // Make sure destination exists
                if (!destination.exists()) { destination.mkdir(); }

                ZipEntry entry = zipIn.getNextEntry();
                // create a hidden directory for the extracted files
                File publicationFolder = new File
                        (destination + "/" + EPUBFile.getName().replaceAll(".epub", ""));
                publicationFolder.mkdirs();

                while (entry != null) {
                    if (unnecessaryFile(entry.getName())) {
                        entry = zipIn.getNextEntry();
                        continue;
                    }
                    String filePath = publicationFolder + File.separator + entry.getName();
                    File entryFile = new File(filePath);

                    if (entry.isDirectory()) { entryFile.mkdir(); }
                    else {
                        /*
                        * because all other folders and files are categorized as unnecessary, the
                         * only parent file (which is a directory) that passes the filter is "OEBPS/"
                        */
                        File OEBPSFolder = entryFile.getParentFile();
                        if (!OEBPSFolder.exists()) {
                            OEBPSFolder.mkdirs();
                            OEBPSFolderPath = OEBPSFolder.getPath();
                        }
                        extractFile(zipIn, entryFile);
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
            removeNonMeetingFiles(new File(OEBPSFolderPath));
            moveContentFilesToPublicationFolder(new File(OEBPSFolderPath));
        }
    }

    private void removeNonMeetingFiles(File OEBPSFolder) throws IOException {
        File[] XHTMLFiles = OEBPSFolder.listFiles();

        for (File XHTMLFile : Objects.requireNonNull(XHTMLFiles)) {
            Document XHTMLDocument = Jsoup.parse(XHTMLFile, "UTF-8");
            if (
                    !XHTMLDocument.html().contains("treasures") &&
                    !XHTMLDocument.html().contains("ministry") &&
                    !XHTMLDocument.html().contains("christianLiving")
            )
                XHTMLFile.delete();
        }
    }

    private void moveContentFilesToPublicationFolder (File OEBPSFolder) {
        for (File XHTMLFile : Objects.requireNonNull(OEBPSFolder.listFiles())) {
            XHTMLFile.renameTo(new File(OEBPSFolder.getParent() + '/' + XHTMLFile.getName()));
        }
        // remove the "OEBPS/" folder when done
        OEBPSFolder.delete();
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

    private void extractFile(ZipInputStream zipIn, File file)
            throws IOException {
        try (BufferedOutputStream outputStream = new BufferedOutputStream
                (new FileOutputStream(file))) {
            byte[] buffer = new byte[50];
            int location;
            while ((location = zipIn.read(buffer)) != -1) {
                outputStream.write(buffer, 0, location);
            }
        }
    }
}
