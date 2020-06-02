package com.excel;

import com.domain.Settings;
import com.extraction.ContentParser;

import com.meeting.Meeting;
import com.meeting.MeetingSection;

import com.meeting.SectionKind;
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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Properties;

public class ExcelFileGenerator {
    private final XSSFWorkbook WORKBOOK;
    private final ContentParser CONTENT_PARSER;
    private int COL_INDEX = 1;
    private int ROW_INDEX = 4;
    private final File DESTINATION;
    private final Properties LANGUAGE_PACK;
    private final ArrayList<ArrayList<String>> ALL_MEETINGS_CONTENTS;
    private Settings settings;
    private final XSSFCellStyle PART_STYLE;
    private final XSSFCellStyle LABEL_STYLE;
    private final XSSFCellStyle PRESENTER_NAME_STYLE;
    private final XSSFCellStyle SECTION_TITLE_STYLE;
    private final XSSFCellStyle HALL_DIVIDERS_STYLE;

    public ExcelFileGenerator(
            ArrayList<ArrayList<String>> ALL_MEETINGS_CONTENTS,
            Properties LANGUAGE_PACK,
            File DESTINATION)
    {
        this.ALL_MEETINGS_CONTENTS = ALL_MEETINGS_CONTENTS;
        this.LANGUAGE_PACK         = LANGUAGE_PACK;
        this.DESTINATION           = DESTINATION;
        CONTENT_PARSER             = new ContentParser(this.LANGUAGE_PACK.getProperty("filter_for_minute"));
        WORKBOOK                   = new XSSFWorkbook();

        try {
            settings = Settings.getLastSavedSettings();
        } catch (SQLException e) {
            settings = Settings.getDefaultSettings();
        }

        PART_STYLE = getCellStyle(false, false, settings.getPartFontSize(), false);
        LABEL_STYLE = getCellStyle(false, false, settings.getLabelsFontSize(), false);
        PRESENTER_NAME_STYLE = getCellStyle(false, true, settings.getPresenterNameFontSize(), false);
        SECTION_TITLE_STYLE = getCellStyle(true, false, settings.getMeetingSectionTitleFontSize(), true);
        HALL_DIVIDERS_STYLE = getCellStyle(true, true, settings.getLabelsFontSize(), false);
    }

    private void insertPageTitle(XSSFSheet sheet) {
        String sheetName = sheet.getSheetName(), month, year, fullTitle;
        // data population starts from the 3rd row
        Row row = getRowIfExists(ROW_INDEX - 2, sheet);
        // the last two numbers specify the month of the publication
        month = sheetName.substring(sheetName.length() - 2);
        year  = sheetName.substring(sheetName.length() - 6, sheetName.length() - 2);
        fullTitle = LANGUAGE_PACK.getProperty("meeting_name") + " – " +
                LANGUAGE_PACK.getProperty(month) + " " + year;
        // set the header of the page
        row.createCell(COL_INDEX).setCellValue(fullTitle);
        row.getCell(COL_INDEX).setCellStyle(getCellStyle
                (false, true, settings.getSheetTitleFontSize(), true));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), COL_INDEX, COL_INDEX + 8));
    }

    private void insertHeaderSection(String weekSpan, XSSFSheet sheet) {
        // 5th row has "week span" in it
        Row row = getRowIfExists(ROW_INDEX, sheet);
        row.getCell(COL_INDEX).setCellValue(weekSpan);
        row.getCell(COL_INDEX).setCellStyle(getCellStyle
                (false, false, settings.getLabelsFontSize(),true));
        sheet.setColumnWidth(COL_INDEX, 1250);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), COL_INDEX, COL_INDEX + 2));
        // 6th row has the chairman's name
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(COL_INDEX).setCellValue(LANGUAGE_PACK.getProperty("chairman"));
        row.getCell(COL_INDEX).setCellStyle(LABEL_STYLE);
        row.getCell(COL_INDEX + 3).setCellStyle(PRESENTER_NAME_STYLE);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), COL_INDEX, COL_INDEX + 2));
        addThinBordersToCellsInRow(row, COL_INDEX, true);
        // 7th row has the name of the brother who does the opening prayer
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(COL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("opening_prayer"));
        row.getCell(COL_INDEX + 2).setCellStyle(LABEL_STYLE);
        row.getCell(COL_INDEX + 3).setCellStyle(PRESENTER_NAME_STYLE);
    }

    private void insertTreasuresParts(MeetingSection treasures, XSSFSheet sheet) {
        // 8th row has the title of the "Treasures" section
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle(sheet, treasures, row);
        // 10 minute talk, digging for spiritual gems and bible reading
        for (String part : treasures.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);

            if (part.contains(LANGUAGE_PACK.getProperty("bible_reading")) && settings.hasHallDividers()) {
                insertHallDivisionHeaders(row);
                row = getRowIfExists(++ROW_INDEX, sheet);
                insertPart(sheet, part, true, row);
            } else {
                insertPart(sheet, part, false, row);
            }

            addThinBordersToCellsInRow(row, COL_INDEX + 1, true);
        }
    }

    private void insertMinistryParts(MeetingSection improveInMinistry, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle(sheet, improveInMinistry, row);

        if (settings.hasHallDividers()) {
            insertHallDivisionHeaders(row);
        }
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            insertPart(sheet, part, settings.hasHallDividers(), row);
            addThinBordersToCellsInRow(row, COL_INDEX + 1, true);
        }
    }

    private void insertPart(XSSFSheet sheet, String part, boolean hasHallDivision, Row row) {
        row.getCell(COL_INDEX + 1).setCellValue(part);
        row.getCell(COL_INDEX + 1).setCellStyle(PART_STYLE);

        if (!hasHallDivision) {
            sheet.addMergedRegion(new CellRangeAddress
                    (row.getRowNum(), row.getRowNum(), COL_INDEX + 1, COL_INDEX + 2));
        } else {
            // If there is no hall dividing header (Main Hall, Second Hall), then the last two
            // cells must be formatted with the `PRESENTER_NAME_STYLE` cellStyle. The cellStyle
            // assignment at the end of this method ensures that the last cell always get's the
            // PRESENTER_NAME_STYLE cellStyle regardless of hall dividing headers.
            row.getCell(COL_INDEX + 2).setCellStyle(PRESENTER_NAME_STYLE);
        }

        row.getCell(COL_INDEX + 3).setCellStyle(PRESENTER_NAME_STYLE);
    }

    private void insertHallDivisionHeaders(Row row) {
        row.getCell(COL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("main_hall"));
        row.getCell(COL_INDEX + 2).setCellStyle(HALL_DIVIDERS_STYLE);
        row.getCell(COL_INDEX + 3).setCellValue(LANGUAGE_PACK.getProperty("second_hall"));
        row.getCell(COL_INDEX + 3).setCellStyle(HALL_DIVIDERS_STYLE);
    }

    private void insertChristianLifeParts(MeetingSection livingAsChristians, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle(sheet, livingAsChristians, row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            insertPart(sheet, part, false, row);
            addThinBordersToCellsInRow(row, COL_INDEX + 1, true);
        }
    }

    private void insertSectionTitle(Sheet sheet, MeetingSection meetingSection, Row row) {
        final int LAST_COL;

        if (meetingSection.getSECTION_KIND() == SectionKind.IMPROVE_IN_MINISTRY && settings.hasHallDividers()) {
            LAST_COL = COL_INDEX + 1;
        } else {
            LAST_COL = COL_INDEX + 3;
        }

        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), COL_INDEX, LAST_COL));
        row.getCell(COL_INDEX).setCellValue(meetingSection.getSectionTitle());
        row.getCell(COL_INDEX).setCellStyle(SECTION_TITLE_STYLE);
        addThinBordersToCellsInRow(row, COL_INDEX, false);
    }

    private void insertFooterSection(XSSFSheet sheet) {
        // Congregation Bible study reader row
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(COL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("reader"));
        row.getCell(COL_INDEX + 2).setCellStyle(LABEL_STYLE);
        row.getCell(COL_INDEX + 3).setCellStyle(PRESENTER_NAME_STYLE);
        // closing prayer row
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(COL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("concluding_prayer"));
        row.getCell(COL_INDEX + 2).setCellStyle(LABEL_STYLE);
        row.getCell(COL_INDEX + 3).setCellStyle(PRESENTER_NAME_STYLE);
        ROW_INDEX += 3;
    }

    private void addPopulatedSheet(ArrayList<String> meetingFilesContents) {
        /* The first element in `meetingFilesContents` is the name of the publication.
           The publication name is important to name the excel sheets. This first element
           must be removed after it is used */
        XSSFSheet sheet = WORKBOOK.createSheet
                (meetingFilesContents.remove(0).replaceAll("\\.[e|E][p|P][u|U][b|B]", ""));

        CONTENT_PARSER.setMeetingContents(meetingFilesContents);

        int meetingCount = 0;

        insertPageTitle(sheet);
        for (Meeting meeting : CONTENT_PARSER.getMeetings()) {
            if (meetingCount == 3) {
                COL_INDEX = 6;
                ROW_INDEX = 4;
            }

            insertHeaderSection(meeting.getWEEK_SPAN(), sheet);
            insertTreasuresParts(meeting.getTREASURES(), sheet);
            insertMinistryParts(meeting.getIMPROVE_IN_MINISTRY(), sheet);
            insertChristianLifeParts(meeting.getLIVING_AS_CHRISTIANS(), sheet);
            insertFooterSection(sheet);

            ++meetingCount;
        }
        // reset indexes
        COL_INDEX = 1;
        ROW_INDEX = 4;
        // finalize page properties and look
        resizeColumnsAndFixPageSize(sheet);
    }

    public boolean makeExcel(String fileName) throws IOException {
        for (ArrayList<String> meetingContents : ALL_MEETINGS_CONTENTS) {
            addPopulatedSheet(meetingContents);
        }

        FileOutputStream out = new FileOutputStream(new File
                (DESTINATION.getPath() + File.separator + fileName));
        WORKBOOK.write(out);
        out.close();

        return true;
    }

    private Row getRowIfExists(int rowIndex, XSSFSheet sheet) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        for (int column = COL_INDEX; column <= COL_INDEX + 3; ++column) {
            row.createCell(column);
        }

        row.setHeight((short) 600);

        return row;
    }

    private XSSFCellStyle getCellStyle(boolean backgroundColor, boolean centeredText, int fontSize, boolean boldFont) {
        XSSFCellStyle cellStyle = WORKBOOK.createCellStyle();

        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        if (backgroundColor) {
            final byte[] RGB = {(byte) 200, (byte) 200, (byte) 200};
            cellStyle.setFillBackgroundColor(new XSSFColor(RGB, null));
            cellStyle.setFillForegroundColor(new XSSFColor(RGB, null));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        cellStyle.setAlignment(centeredText ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);

        XSSFFont font = WORKBOOK.createFont();
        font.setBold(boldFont);
        font.setFontHeight(fontSize);
        cellStyle.setFont(font);

        return cellStyle;
    }

    private void addThinBordersToCellsInRow(Row row, int startColumn, boolean bottomBorder) {
        for (int columnIndex = startColumn; columnIndex < COL_INDEX + 4; ++columnIndex) {
            if (bottomBorder) {
                row.getCell(columnIndex).getCellStyle().setBorderBottom(BorderStyle.THIN);
            } else {
                row.getCell(columnIndex).getCellStyle().setBorderTop(BorderStyle.THIN);
            }
        }
    }

    private void resizeColumnsAndFixPageSize(XSSFSheet sheet) {
        final double MARGIN_LENGTH = 0.393701; // 0.393701 inch = 1 cm
        final int FIRST_COLUMN = 1;
        final int LAST_COLUMN = 9;

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
}
