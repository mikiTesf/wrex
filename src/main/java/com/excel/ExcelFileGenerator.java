package com.excel;

import com.extraction.ContentParser;
import com.meeting.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelFileGenerator {
    private XSSFWorkbook workbook;
    private XSSFRichTextString formattedText;
    private XSSFFont boldFont;
    private ContentParser contentParser;
    private int CELL_INDEX = 1;
    private int ROW_INDEX = 4;
    private final File cacheFolder = new File(".content/");
    private final File destination;

    public ExcelFileGenerator(File destination) {
        this.destination = destination;
        contentParser = new ContentParser();
        workbook = new XSSFWorkbook();
        boldFont = new XSSFFont();
        boldFont.setBold(true);
    }

    private void insertPageTitle(XSSFSheet sheet) {
        String sheetName = sheet.getSheetName(), month, year, fullTitle;
        // data population starts from the 3rd row
        Row row = getRowIfExists(ROW_INDEX - 2, sheet, true);
        // the last two numbers specify the month of the publication
        month = sheetName.substring(sheetName.length() - 2);
        year  = sheetName.substring(sheetName.length() - 6, sheetName.length() - 2);
        fullTitle = AdditionalStrings.MEETING_NAME + " – " + AdditionalStrings.MONTHS.get(month) + " " + year;

        formattedText = new XSSFRichTextString();
        formattedText.setString(fullTitle);
        formattedText.applyFont(boldFont);
        // set the header of the page
        row.createCell(CELL_INDEX).setCellValue(formattedText);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false, true, false));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 8));
    }

    private void insertHeaderSection(String weekSpan, XSSFSheet sheet) {
        // 5th row has "week span" in it
        Row row = getRowIfExists(ROW_INDEX, sheet, false);
        formattedText.setString(weekSpan);
        formattedText.applyFont(boldFont);
        row.getCell(CELL_INDEX).setCellValue(formattedText);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false, false, true));
        // 6th row has the chairman's name
        row = getRowIfExists(++ROW_INDEX, sheet, false);
        formattedText.setString(AdditionalStrings.CHAIRMAN);
        formattedText.applyFont(boldFont);
        row.getCell(CELL_INDEX).setCellValue(formattedText);
        setBottomBorderedCellStyle(row, CELL_INDEX, CELL_INDEX + 3);
        // 7th row has the name of the brother who does the opening prayer
        row = getRowIfExists(++ROW_INDEX, sheet, false);
        formattedText.setString(AdditionalStrings.OPENING_PRAYER);
        row.getCell(CELL_INDEX + 2).setCellValue(formattedText);
        setBottomBorderedCellStyle(row, CELL_INDEX + 2, CELL_INDEX + 2);
    }

    private void insertTreasuresParts(MeetingSection treasures, XSSFSheet sheet) {
        // 8th row has the title of the "Treasures" section
        Row row = getRowIfExists(++ROW_INDEX, sheet, false);
        insertSectionTitle
                (sheet, treasures.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 3);
        // 10 minute talk, digging for spiritual gems and bible reading
        for (String part : treasures.getParts()) {
            if (part.contains(AdditionalStrings.BIBLE_READING)) {
                row = getRowIfExists(++ROW_INDEX, sheet, false);
                insertHallDivisionHeader(row);
            }
            row = getRowIfExists(++ROW_INDEX, sheet, true);
            if (!part.contains(AdditionalStrings.BIBLE_READING)) {
                sheet.addMergedRegion(new CellRangeAddress
                        (row.getRowNum(), row.getRowNum(), CELL_INDEX + 1, CELL_INDEX + 2));
            }
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle(row, CELL_INDEX + 1, CELL_INDEX + 3);
        }
    }

    private void insertMinistryParts(MeetingSection improveInMinistry, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet, false);
        insertSectionTitle
                (sheet, improveInMinistry.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 1);
        insertHallDivisionHeader(row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet, true);
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle(row, CELL_INDEX + 1, CELL_INDEX + 3);
        }
    }

    private void insertHallDivisionHeader(Row row) {
        row.getCell(CELL_INDEX + 2).setCellValue(AdditionalStrings.MAIN_HALL);
        row.getCell(CELL_INDEX + 2).setCellStyle(getCellStyle
                (true, true, false, true, true));
        row.getCell(CELL_INDEX + 3).setCellValue(AdditionalStrings.SECOND_HALL);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (true, true, false, true, true));
    }

    private void insertChristianLifeParts(MeetingSection livingAsChristians, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet, false);
        insertSectionTitle
                (sheet, livingAsChristians.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 3);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet, true);
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle(row, CELL_INDEX + 1, CELL_INDEX + 3);
            sheet.addMergedRegion(new CellRangeAddress
                    (row.getRowNum(), row.getRowNum(), CELL_INDEX + 1, CELL_INDEX + 2));
        }
    }

    private void insertSectionTitle(
            Sheet sheet,
            String sectionTitle,
            Row row,
            int beginCol,
            int endCol
    ) {
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), beginCol, endCol));
        formattedText.setString(sectionTitle);
        formattedText.applyFont(boldFont);
        row.getCell(beginCol).setCellValue(formattedText);
        row.getCell(beginCol).setCellStyle(getCellStyle
                (true, true, false, false, false));
    }

    private void insertFooterSection(XSSFSheet sheet) {
        // Congregation Bible study reader row
        Row row = getRowIfExists(++ROW_INDEX, sheet, false);
        row.getCell(CELL_INDEX + 2).setCellValue(AdditionalStrings.READER);
        setBottomBorderedCellStyle(row, CELL_INDEX + 2, CELL_INDEX + 3);
        // closing prayer row
        row = getRowIfExists(++ROW_INDEX, sheet, false);
        row.getCell(CELL_INDEX + 2).setCellValue(AdditionalStrings.CONCLUDING_PRAYER);
        setBottomBorderedCellStyle(row, CELL_INDEX + 2, CELL_INDEX + 3);
        ROW_INDEX += 3;
    }

    private void addPopulatedSheet(File publicationFolder) {
        contentParser.setPublicationFolder(publicationFolder);
        contentParser.readRawHTML();

        XSSFSheet sheet = workbook.createSheet(publicationFolder.getName());

        int meetingCount = 0;

        insertPageTitle(sheet);
        for (Meeting meeting : contentParser.getMeetings()) {
            if (meetingCount == 3) {
                CELL_INDEX = 6;
                ROW_INDEX = 4;
            }
            insertHeaderSection(meeting.getWeekSpan(), sheet);
            insertTreasuresParts(meeting.getTreasures(), sheet);
            insertMinistryParts(meeting.getImproveInMinistry(), sheet);
            insertChristianLifeParts(meeting.getLivingAsChristians(), sheet);
            insertFooterSection(sheet);
            ++meetingCount;
        }
        // reset indexes
        CELL_INDEX = 1;
        ROW_INDEX = 4;
        // finalize page properties and look
        resizeColumnsAndFixPageSize(sheet);
    }

    public int makeExcel(String fileName) {
        File[] publicationFolders = cacheFolder.listFiles();

        if (publicationFolders == null) { return 1; }

        for (File publicationFolder : publicationFolders) {
            addPopulatedSheet(publicationFolder);
        }

        try {
            FileOutputStream out = new FileOutputStream(new File
                    (destination.getPath() + "/" + fileName));
            workbook.write(out);
            out.close();
        } catch (IOException e) { return 2; }

        removeCacheAndPublicationFolders();
        return 0;
    }

    private Row getRowIfExists(int rowIndex, XSSFSheet sheet, boolean increaseRowHeight) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        for (int column = CELL_INDEX; column <= CELL_INDEX + 3; ++column) {
            row.createCell(column);
        }

        if (increaseRowHeight) row.setHeight((short) 400);

        return row;
    }

    private XSSFCellStyle getCellStyle(
            boolean backgroundColor,
            boolean topBordered,
            boolean bottomBordered,
            boolean centeredText,
            boolean smallerFont
    ) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        if (backgroundColor) {
            final byte[] RGB = {(byte) 200, (byte) 200, (byte) 200};
            cellStyle.setFillBackgroundColor(new XSSFColor(RGB, new DefaultIndexedColorMap()));
            cellStyle.setFillForegroundColor(new XSSFColor(RGB, new DefaultIndexedColorMap()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (topBordered) cellStyle.setBorderTop(BorderStyle.THIN);
        if (bottomBordered) cellStyle.setBorderBottom(BorderStyle.THIN);

        cellStyle.setAlignment(centeredText ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);

        XSSFFont font = workbook.createFont();
        font.setFontHeight(smallerFont? 12 : 14);
        cellStyle.setFont(font);

        return cellStyle;
    }

    private void setBottomBorderedCellStyle(Row row, int firstColumn, int lastColumn) {
        for (int column = firstColumn; column <= lastColumn; ++column) {
            row.getCell(column).setCellStyle(getCellStyle
                    (false, false, true, false, true));
        }
    }

    private void resizeColumnsAndFixPageSize(XSSFSheet sheet) {
        final double MARGIN_LENGTH = 0.1; // 0.393701 in = 1 cm
        final int FIRST_COLUMN = 1;
        final int LAST_COLUMN = 7;

        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setMargin(Sheet.LeftMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.RightMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.TopMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.BottomMargin, MARGIN_LENGTH);
        sheet.setFitToPage(true);

        for (int column = FIRST_COLUMN; column <= LAST_COLUMN; column++) {
            sheet.autoSizeColumn(column, false);
        }
    }

    private void removeCacheAndPublicationFolders() {
        // noinspection ConstantConditions
        for (File publicationFolder : cacheFolder.listFiles()) {
            // noinspection ConstantConditions
            for (File XHTMLFile : publicationFolder.listFiles()) {
                // noinspection ResultOfMethodCallIgnored
                XHTMLFile.delete();
            }
            // noinspection ResultOfMethodCallIgnored
            publicationFolder.delete();
        }
        // noinspection ResultOfMethodCallIgnored
        cacheFolder.delete();
    }
}
