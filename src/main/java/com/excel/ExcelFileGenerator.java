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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class ExcelFileGenerator {
    private final XSSFWorkbook WORKBOOK;
    private final ContentParser CONTENT_PARSER;
    private int CELL_INDEX = 1;
    private int ROW_INDEX  = 4;
    private final File DESTINATION;
    private final Properties LANGUAGE_PACK;
    private final ArrayList<ArrayList<String>> ALL_MEETINGS_CONTENTS;

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
        row.createCell(CELL_INDEX).setCellValue(fullTitle);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false,
                        true, false, true));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 8));
    }

    private void insertHeaderSection(String weekSpan, XSSFSheet sheet) {
        // 5th row has "week span" in it
        Row row = getRowIfExists(ROW_INDEX, sheet);
        row.getCell(CELL_INDEX).setCellValue(weekSpan);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false,
                        false, false, true));
        sheet.setColumnWidth(CELL_INDEX, 1250);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 2));
        // 6th row has the chairman's name
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX).setCellValue(LANGUAGE_PACK.getProperty("chairman"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX, CELL_INDEX + 3, true, false, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle(
                false, false, true, true, false, false
        ));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 2));
        // 7th row has the name of the brother who does the opening prayer
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("opening_prayer"));
        setBottomBorderedCellStyle
	            (row, CELL_INDEX, CELL_INDEX + 3, false, true, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (false, false, true, true, false, false));
    }

    private void insertTreasuresParts(MeetingSection treasures, XSSFSheet sheet) {
        // 8th row has the title of the "Treasures" section
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle
                (sheet, treasures.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 3);
        // 10 minute talk, digging for spiritual gems and bible reading
        for (String part : treasures.getParts()) {
            if (part.contains(LANGUAGE_PACK.getProperty("bible_reading"))) {
                row = getRowIfExists(++ROW_INDEX, sheet);
                insertHallDivisionHeader(row);
            }
            row = getRowIfExists(++ROW_INDEX, sheet);
            if (!part.contains(LANGUAGE_PACK.getProperty("bible_reading"))) {
                sheet.addMergedRegion(new CellRangeAddress
                        (row.getRowNum(), row.getRowNum(), CELL_INDEX + 1, CELL_INDEX + 2));
            }
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle
                    (row, CELL_INDEX + 1, CELL_INDEX + 3, false, false, 2);
        }
    }

    private void insertMinistryParts(MeetingSection improveInMinistry, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle
                (sheet, improveInMinistry.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 1);
        insertHallDivisionHeader(row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle
                    (row, CELL_INDEX + 1, CELL_INDEX + 3, false, false, 2);
        }
    }

    private void insertHallDivisionHeader(Row row) {
        row.getCell(CELL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("main_hall"));
        row.getCell(CELL_INDEX + 2).setCellStyle(getCellStyle
                (true, true, false,
                        true, true, false));
        row.getCell(CELL_INDEX + 3).setCellValue(LANGUAGE_PACK.getProperty("second_hall"));
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (true, true, false,
                        true, true, false));
    }

    private void insertChristianLifeParts(MeetingSection livingAsChristians, XSSFSheet sheet) {
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        insertSectionTitle
                (sheet, livingAsChristians.getSectionTitle(), row, CELL_INDEX, CELL_INDEX + 3);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = getRowIfExists(++ROW_INDEX, sheet);
            row.getCell(CELL_INDEX + 1).setCellValue(part);
            setBottomBorderedCellStyle
                    (row, CELL_INDEX + 1, CELL_INDEX + 3, false, false, 1);
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
        row.getCell(beginCol).setCellValue(sectionTitle);
        row.getCell(beginCol).setCellStyle(getCellStyle
                (true, true, false,
                        false, false, true));
    }

    private void insertFooterSection(XSSFSheet sheet) {
        // Congregation Bible study reader row
        Row row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("reader"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX + 2, CELL_INDEX + 3, false, true, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (false, false, true, true, false, false));
        // closing prayer row
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX + 2).setCellValue(LANGUAGE_PACK.getProperty("concluding_prayer"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX + 2, CELL_INDEX + 3, false, true, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (false, false, true, true, false, false));
        ROW_INDEX += 3;
    }

    private void addPopulatedSheet(ArrayList<String> meetingFilesContents) {
        /* The first element in `meetingFilesContents` is the name of the publication.
           The publication name is important to name the excel sheets. This first element
           must be removed after it is used */
        XSSFSheet sheet = WORKBOOK.createSheet
                (meetingFilesContents.remove(0).replaceAll("\\.[e|E][p|P][u|U][b|B]", ""));

        CONTENT_PARSER.setMeetingContents(meetingFilesContents);
        CONTENT_PARSER.parseXHTML();

        int meetingCount = 0;

        insertPageTitle(sheet);
        for (Meeting meeting : CONTENT_PARSER.getMeetings()) {
            if (meetingCount == 3) {
                CELL_INDEX = 6;
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
        CELL_INDEX = 1;
        ROW_INDEX = 4;
        // finalize page properties and look
        resizeColumnsAndFixPageSize(sheet);
    }

    public int makeExcel(String fileName) {

        if (ALL_MEETINGS_CONTENTS.size() == 0) return 3;

        for (ArrayList<String> meetingContents : ALL_MEETINGS_CONTENTS) {
            addPopulatedSheet(meetingContents);
        }

        try {
            FileOutputStream out = new FileOutputStream(new File
                    (DESTINATION.getPath() + File.separator + fileName));
            WORKBOOK.write(out);
            out.close();
        } catch (IOException e) { return 4; }

        return 0;
    }

    private Row getRowIfExists(int rowIndex, XSSFSheet sheet) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        for (int column = CELL_INDEX; column <= CELL_INDEX + 3; ++column) {
            row.createCell(column);
        }

        row.setHeight((short) 600);

        return row;
    }

    private XSSFCellStyle getCellStyle(
            boolean backgroundColor,
            boolean topBordered,
            boolean bottomBordered,
            boolean centeredText,
            boolean smallerFont,
            boolean boldFont
    ) {
        XSSFCellStyle cellStyle = WORKBOOK.createCellStyle();

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

        XSSFFont font = WORKBOOK.createFont();
        font.setBold(boldFont);
        font.setFontHeight(smallerFont? 15 : 16);
        cellStyle.setFont(font);

        return cellStyle;
    }

    private void setBottomBorderedCellStyle (
            Row row,
            int firstColumn,
            int lastColumn,
            boolean boldFont,
            boolean smallerFont,
            int lastCellsToCenterHorizontally
    ) {
        for (int column = firstColumn; column <= lastColumn; ++column) {
            row.getCell(column).setCellStyle(getCellStyle
                    (false, false, true,
                            false, smallerFont, boldFont));
        }

        for (int column = row.getLastCellNum() - 1; lastCellsToCenterHorizontally > 0; --column) {
            row.getCell(column).getCellStyle().setAlignment(HorizontalAlignment.CENTER);
            --lastCellsToCenterHorizontally;
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
