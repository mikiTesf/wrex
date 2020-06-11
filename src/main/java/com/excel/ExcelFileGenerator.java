package com.excel;

import com.domain.Settings;

import com.meeting.Meeting;
import com.meeting.MeetingSection;

import com.meeting.Part;
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

import java.util.ArrayList;
import java.util.Properties;

public class ExcelFileGenerator {
    private final XSSFWorkbook WORKBOOK;
    private int COL_INDEX = 1;
    private int ROW_INDEX = 4;
    private final File DESTINATION;
    private final Properties LANGUAGE_PACK;
    private final Settings SETTINGS;
    private final XSSFCellStyle PART_STYLE;
    private final XSSFCellStyle LABEL_STYLE;
    private final XSSFCellStyle PRESENTER_NAME_STYLE;
    private final XSSFCellStyle SECTION_TITLE_STYLE;
    private final XSSFCellStyle HALL_DIVIDERS_STYLE;

    public ExcelFileGenerator(
            Properties LANGUAGE_PACK,
            File DESTINATION)
    {
        this.LANGUAGE_PACK = LANGUAGE_PACK;
        this.DESTINATION   = DESTINATION;
        WORKBOOK           = new XSSFWorkbook();
        SETTINGS = Settings.getLastSavedSettings();
        PART_STYLE = getCellStyle(false, false, SETTINGS.getPartFontSize(), false);
        LABEL_STYLE = getCellStyle(false, false, SETTINGS.getLabelsFontSize(), false);
        PRESENTER_NAME_STYLE = getCellStyle(false, true, SETTINGS.getPresenterNameFontSize(), false);
        SECTION_TITLE_STYLE = getCellStyle(true, false, SETTINGS.getMeetingSectionTitleFontSize(), true);
        HALL_DIVIDERS_STYLE = getCellStyle(true, true, SETTINGS.getLabelsFontSize(), false);
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
                (false, true, SETTINGS.getSheetTitleFontSize(), true));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), COL_INDEX, COL_INDEX + 8));
    }

    private void insertHeaderSection(String weekSpan, XSSFSheet sheet) {
        // 5th row has "week span" in it
        Row row = getRowIfExists(ROW_INDEX, sheet);
        row.getCell(COL_INDEX).setCellValue(weekSpan);
        row.getCell(COL_INDEX).setCellStyle(getCellStyle
                (false, false, SETTINGS.getLabelsFontSize(),true));
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
        String partTitle;
        // 10 minute talk, digging for spiritual gems and bible reading
        for (Part part : treasures.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            partTitle = part.getPartTitle();

            if (partTitle.contains(LANGUAGE_PACK.getProperty("bible_reading")) &&
                    SETTINGS.hasHallDividers())
            {
                insertHallDivisionHeaders(row);
                row = getRowIfExists(++ROW_INDEX, sheet);
                insertPart(sheet, partTitle, true, row);
            } else {
                insertPart(sheet, partTitle, false, row);
            }

            addThinBordersToCellsInRow(row, COL_INDEX + 1, true);
        }
    }

    private void insertMinistryParts(MeetingSection improveInMinistry, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle(sheet, improveInMinistry, row);

        if (SETTINGS.hasHallDividers()) {
            insertHallDivisionHeaders(row);
        }
        // the number of parts is not fixed for all months hence the for loop
        for (Part part : improveInMinistry.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            insertPart(sheet, part.getPartTitle(), SETTINGS.hasHallDividers(), row);
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
            // If there is a hall dividing header (Main Hall, Second Hall), then the last two
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
        for (Part part : livingAsChristians.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            insertPart(sheet, part.getPartTitle(), false, row);
            addThinBordersToCellsInRow(row, COL_INDEX + 1, true);
        }
    }

    private void insertSectionTitle(Sheet sheet, MeetingSection meetingSection, Row row) {
        final int LAST_COL;

        if (meetingSection.getSECTION_KIND() == SectionKind.IMPROVE_IN_MINISTRY && SETTINGS.hasHallDividers()) {
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

    public void addPopulatedSheet(ArrayList<Meeting> meetings, String publicationName) {
        XSSFSheet sheet = WORKBOOK.createSheet(publicationName);

        int meetingCount = 0;

        insertPageTitle(sheet);
        for (Meeting meeting : meetings) {
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

    public void saveExcelDocument(String fileName) throws IOException {
        FileOutputStream out = new FileOutputStream(new File
                (DESTINATION.getPath() + File.separator + fileName));
        WORKBOOK.write(out);
        out.close();
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
        final int LAST_COLUMN = sheet.getRow(ROW_INDEX).getLastCellNum();

        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setMargin(Sheet.LeftMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.RightMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.TopMargin, MARGIN_LENGTH);
        sheet.setMargin(Sheet.BottomMargin, MARGIN_LENGTH);
        sheet.setFitToPage(true);

        for (int column = COL_INDEX; column <= LAST_COLUMN; column++) {
            sheet.autoSizeColumn(column, false);
        }
    }
}
