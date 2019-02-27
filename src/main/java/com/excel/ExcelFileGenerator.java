package com.excel;

import com.extraction.ContentParser;
import com.meeting.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelFileGenerator {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFRichTextString formattedText;
    private XSSFFont boldFont;
    private ContentParser contentParser;
    private int CELL_INDEX = 1;
    private int ROW_INDEX = 4;

    public ExcelFileGenerator(File publicationFolder) {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("wrex_01");
        boldFont = new XSSFFont();
        boldFont.setBold(true);
        contentParser = new ContentParser(publicationFolder);
    }

    private void insertPageTitle() {
        // data population starts from the 3rd row
        Row row = sheet.createRow(2);

        formattedText = new XSSFRichTextString();
        formattedText.setString("ክርስቲያናዊ ህይወታችንና አገልግሎታችን");
        formattedText.applyFont(boldFont);
        // set the header of the page
        row.createCell(CELL_INDEX).setCellValue(formattedText);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false, true, false));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 6));
    }

    private void insertHeaderSection(String weekSpan) {
        // 4th (index -> 3) row is free
        // 5th row has "week span" in it
        Row row = getRowIfExists(ROW_INDEX);
        formattedText.setString(weekSpan);
        formattedText.applyFont(boldFont);
        row.createCell(CELL_INDEX).setCellValue(formattedText);
        // 6th row has the chairman's name
        row = getRowIfExists(++ROW_INDEX);
        formattedText.setString("ሊቀመንበር");
        formattedText.applyFont(boldFont);
        row.createCell(CELL_INDEX).setCellValue(formattedText);
        // 7th row has the name of the brother who does the opening prayer
        row = getRowIfExists(++ROW_INDEX);
        formattedText.setString("የመክፈቻ ፀሎት");
        row.createCell(CELL_INDEX + 1).setCellValue(formattedText);
    }

    private void insertTreasuresParts(Treasures treasures) {
        // 8th row has the title of the "Treasures" section
        Row row = getRowIfExists(++ROW_INDEX);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 2));
        insertSectionTitle("ከአምላክ ቃል የሚገኝ ውድ ሀብት" ,row);
        // 10 minute talk, digging for spiritual gems and bible reading
        for (String part : treasures.getParts()) {
            if (part.contains("የመጽሐፍ ቅዱስ ንባብ")) {
                row = getRowIfExists(++ROW_INDEX);
                insertHallDivisionHeaders(row);
            }
            row = getRowIfExists(++ROW_INDEX);
            if (!part.contains("የመጽሐፍ ቅዱስ ንባብ")) {
                sheet.addMergedRegion(new CellRangeAddress
                        (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 1));
            }
            row.createCell(CELL_INDEX).setCellValue(part);
            row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false, false));
        }
    }

    private void insertMinistryParts(ImproveInMinistry improveInMinistry) {
        Row row = getRowIfExists(++ROW_INDEX);
        insertSectionTitle("በአገልግሎት ውጤታማ ለመሆን ተጣጣር", row);
        insertHallDivisionHeaders(row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row.createCell(CELL_INDEX).setCellValue(part);
            row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false, false));
        }
    }

    private void insertSectionTitle(String sectionTitle, Row row) {
        formattedText.setString(sectionTitle);
        formattedText.applyFont(boldFont);
        row.createCell(CELL_INDEX).setCellValue(formattedText);
        row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                (true, true, false, false, false));
    }

    private void insertHallDivisionHeaders(Row row) {
        row.createCell(CELL_INDEX + 1).setCellValue("በዋናው አዳራሽ");
        row.getCell(CELL_INDEX + 1).setCellStyle(getCellStyle
                (true, true, false, true, true));
        row.createCell(CELL_INDEX + 2).setCellValue("በሁለተኛው አዳራሽ");
        row.getCell(CELL_INDEX + 2).setCellStyle(getCellStyle
                (true, true, false, true, true));
    }

    private void insertChristianLifeParts(LivingAsChristians livingAsChristians) {
        Row row = getRowIfExists(++ROW_INDEX);
        insertSectionTitle("ክርስቲያናዊ ህይወት", row);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 2));
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = getRowIfExists(++ROW_INDEX);
            row.createCell(CELL_INDEX).setCellValue(part);
            sheet.addMergedRegion(new CellRangeAddress
                    (row.getRowNum(), row.getRowNum(), CELL_INDEX, CELL_INDEX + 1));
            row.getCell(CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false ,false));
        }
    }

    private void insertFooterSection() {
        // Congregation Bible study reader row
        Row row = getRowIfExists(++ROW_INDEX);
        row.createCell(CELL_INDEX + 1).setCellValue("አንባቢ");
        row.getCell(CELL_INDEX + 1).setCellStyle(getCellStyle
                (false, false, true, false, false));
        // closing prayer row
        row = getRowIfExists(++ROW_INDEX);
        row.createCell(CELL_INDEX + 1).setCellValue("ፀሎት");
        row.getCell(CELL_INDEX + 1).setCellStyle(getCellStyle
                (false, false, true, false, false));
        ROW_INDEX += 3;
    }

    public void makeExcel() {
        int meetingCount = 0;
        insertPageTitle();
        for (Meeting meeting : contentParser.getMeetings()) {
            if (meetingCount == 3){
                CELL_INDEX = 5;
                ROW_INDEX = 4;
            }
            insertHeaderSection(meeting.getWeekSpan());
            for (MeetingSection meetingSection : meeting.getSections()) {
                switch (meetingSection.getKind()) {
                    case MeetingSection.TREASURES:
                        insertTreasuresParts((Treasures) meetingSection);
                        break;
                    case MeetingSection.IMPROVE_IN_MINISTRY:
                        insertMinistryParts((ImproveInMinistry) meetingSection);
                        break;
                    case MeetingSection.LIVING_AS_CHRISTIANS:
                        insertChristianLifeParts((LivingAsChristians) meetingSection);
                        break;
                    default:
                        break;
                }
            }
            insertFooterSection();
            ++meetingCount;
        }
        // finalize page properties and look
        resizeColumnsAndFixPageSize();
        // write the document on disk
        try {
            FileOutputStream out = new FileOutputStream(new File("WREX_01.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Row getRowIfExists(int rowIndex) {
        return sheet.getRow(rowIndex) == null ?
                sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
    }

    private XSSFCellStyle getCellStyle(
            boolean backgroundColor,
            boolean topBordered,
            boolean bottomBordered,
            boolean centeredText,
            boolean smallerFont
    ) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        if (backgroundColor) {
            final byte[] RGB = {(byte) 200, (byte) 200, (byte) 200};
            cellStyle.setFillBackgroundColor(new XSSFColor(RGB, new DefaultIndexedColorMap()));
            cellStyle.setFillForegroundColor(new XSSFColor(RGB, new DefaultIndexedColorMap()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (topBordered)    cellStyle.setBorderTop(BorderStyle.THIN);
        if (bottomBordered) cellStyle.setBorderBottom(BorderStyle.THIN);

        cellStyle.setAlignment(centeredText ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);

        if (smallerFont) {
            XSSFFont smallFont = workbook.createFont();
            smallFont.setFontHeight(9);
            cellStyle.setFont(smallFont);
        }

        return cellStyle;
    }

    private void resizeColumnsAndFixPageSize() {
        // finalize page setup
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        for (int column = CELL_INDEX; column < 11; column++) {
            sheet.autoSizeColumn(column);
        }
    }
}
