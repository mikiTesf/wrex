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
import java.util.Properties;

public class ExcelFileGenerator {
    private XSSFWorkbook workbook;
    private ContentParser contentParser;
    private int CELL_INDEX = 1;
    private int ROW_INDEX  = 4;
    private final File cacheFolder = new File(".content/");
    private final File destination;
    private final Properties languagePack;

    public ExcelFileGenerator(File destination, Properties languagePack) {
        this.languagePack = languagePack;
        this.destination = destination;
        contentParser = new ContentParser(this.languagePack.getProperty("filter_for_minute"));
        workbook = new XSSFWorkbook();
    }

    private void insertPageTitle(XSSFSheet sheet) {
        String sheetName = sheet.getSheetName(), month, year, fullTitle;
        // data population starts from the 3rd row
        Row row = getRowIfExists(ROW_INDEX - 2, sheet);
        // the last two numbers specify the month of the publication
        month = sheetName.substring(sheetName.length() - 2);
        year  = sheetName.substring(sheetName.length() - 6, sheetName.length() - 2);
        fullTitle = languagePack.getProperty("meeting_name") + " – " +
                languagePack.getProperty(month) + " " + year;

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
        row.getCell(CELL_INDEX).setCellValue(languagePack.getProperty("chairman"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX, CELL_INDEX + 3, true, false, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle(
                false, false, true, true, false, false
        ));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 2));
        // 7th row has the name of the brother who does the opening prayer
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX + 2).setCellValue(languagePack.getProperty("opening_prayer"));
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
            if (part.contains(languagePack.getProperty("bible_reading"))) {
                row = getRowIfExists(++ROW_INDEX, sheet);
                insertHallDivisionHeader(row);
            }
            row = getRowIfExists(++ROW_INDEX, sheet);
            if (!part.contains(languagePack.getProperty("bible_reading"))) {
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
        row.getCell(CELL_INDEX + 2).setCellValue(languagePack.getProperty("main_hall"));
        row.getCell(CELL_INDEX + 2).setCellStyle(getCellStyle
                (true, true, false,
                        true, true, false));
        row.getCell(CELL_INDEX + 3).setCellValue(languagePack.getProperty("second_hall"));
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
        row.getCell(CELL_INDEX + 2).setCellValue(languagePack.getProperty("reader"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX + 2, CELL_INDEX + 3, false, true, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (false, false, true, true, false, false));
        // closing prayer row
        row = getRowIfExists(++ROW_INDEX, sheet);
        row.getCell(CELL_INDEX + 2).setCellValue(languagePack.getProperty("concluding_prayer"));
        setBottomBorderedCellStyle
                (row, CELL_INDEX + 2, CELL_INDEX + 3, false, true, 1);
        row.getCell(CELL_INDEX + 3).setCellStyle(getCellStyle
                (false, false, true, true, false, false));
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

        if (publicationFolders == null) { return 3; }

        for (File publicationFolder : publicationFolders) {
            addPopulatedSheet(publicationFolder);
        }

        try {
            FileOutputStream out = new FileOutputStream(new File
                    (destination.getPath() + "/" + fileName));
            workbook.write(out);
            out.close();
        } catch (IOException e) { return 4; }

        deleteFile_s(cacheFolder);
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

    private void deleteFile_s(File cacheFolder) {
        if (cacheFolder.isDirectory()) {
            // noinspection ConstantConditions
            for (File file : cacheFolder.listFiles()) {
                deleteFile_s(file);
            }
        }
        // noinspection ResultOfMethodCallIgnored
        cacheFolder.delete();
    }
}
