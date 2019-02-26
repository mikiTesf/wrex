import com.meeting.ImproveInMinistry;
import com.meeting.LivingAsChristians;
import com.meeting.Meeting;
import com.meeting.Treasures;

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

class ExcelFileGenerator {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFRichTextString formattedText;
    private XSSFFont boldFont;
    private ContentParser contentParser;
    private final int BEGIN_CELL_INDEX = 1;

    ExcelFileGenerator(File XHTMLFile) {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        boldFont = new XSSFFont();
        boldFont.setBold(true);
        contentParser = new ContentParser(XHTMLFile);
    }

    private void insertPageTitle() {
        // data population starts from the 3rd row
        Row row = sheet.createRow(2);

        formattedText = new XSSFRichTextString();
        formattedText.setString("ክርስቲያናዊ ህይወታችንና አገልግሎታችን");
        formattedText.applyFont(boldFont);
        // set the header of the page
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false, true, false));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), BEGIN_CELL_INDEX, BEGIN_CELL_INDEX + 9));
    }

    private void insertHeaderSection() {
        // 4th (index -> 3) row is free
        // 5th row has "week span" in it
        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        boldFont.setBold(true);
        formattedText.setString(contentParser.getWeekSpan());
        formattedText.applyFont(boldFont);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        // 6th row has the chairman's name
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        boldFont.setBold(true);
        formattedText.setString("ሊቀመንበር");
        formattedText.applyFont(boldFont);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        // 7th row has the name of the brother who does the opening prayer
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        formattedText.setString("የመክፈቻ ፀሎት");
        row.createCell(BEGIN_CELL_INDEX + 1).setCellValue(formattedText);
    }

    private void insertTreasuresParts(Treasures treasures) {
        // 8th row has the title of the "Treasures" section
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), BEGIN_CELL_INDEX, BEGIN_CELL_INDEX + 2));
        formattedText.setString("ከአምላክ ቃል የሚገኝ ውድ ሀብት");
        formattedText.applyFont(boldFont); // unchanged since last made bold
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (true, true, false, false, false));
        // 10 minute talk, digging for spiritual gems and bible reading
        for (String part : treasures.getParts()) {
            if (part.contains("የመጽሐፍ ቅዱስ ንባብ")) {
                row = sheet.createRow(sheet.getLastRowNum() + 1);
                insertHallDivisionHeaders(row);
            }
            row = sheet.createRow(sheet.getLastRowNum() + 1);
            if (!part.contains("የመጽሐፍ ቅዱስ ንባብ")) {
                sheet.addMergedRegion(new CellRangeAddress
                        (row.getRowNum(), row.getRowNum(), BEGIN_CELL_INDEX, BEGIN_CELL_INDEX + 1));
            }
            row.createCell(BEGIN_CELL_INDEX).setCellValue(part);
            row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false, false));
        }
    }

    private void insertMinistryParts(ImproveInMinistry improveInMinistry) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        formattedText.setString("በአገልግሎት ውጤታማ ለመሆን ተጣጣር");
        formattedText.applyFont(boldFont);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (true, true, false, false, false));
        insertHallDivisionHeaders(row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(BEGIN_CELL_INDEX).setCellValue(part);
            row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false, false));
        }
    }

    private void insertHallDivisionHeaders(Row row) {
        row.createCell(BEGIN_CELL_INDEX + 1).setCellValue("በዋናው አዳራሽ");
        row.getCell(BEGIN_CELL_INDEX + 1).setCellStyle(getCellStyle
                (true, true, false, true, true));
        row.createCell(BEGIN_CELL_INDEX + 2).setCellValue("በሁለተኛው አዳራሽ");
        row.getCell(BEGIN_CELL_INDEX + 2).setCellStyle(getCellStyle
                (true, true, false, true, true));
    }

    private void insertChristianLifeParts(LivingAsChristians livingAsChristians) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        formattedText.setString("ክርስቲያናዊ ህይወት");
        formattedText.applyFont(boldFont);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (true, true, false, false, false));
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), BEGIN_CELL_INDEX, BEGIN_CELL_INDEX + 2));
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(BEGIN_CELL_INDEX).setCellValue(part);
            sheet.addMergedRegion(new CellRangeAddress
                    (row.getRowNum(), row.getRowNum(), BEGIN_CELL_INDEX, BEGIN_CELL_INDEX + 1));
            row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                    (false, false, true, false ,false));
        }
    }

    private void fillFooterSection() {
        // Congregation Bible study reader row
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX);
        row.createCell(row.getLastCellNum()).setCellValue("አንባቢ");
        row.getCell(row.getLastCellNum() - 1).setCellStyle(getCellStyle
                (false, false, true, false, false));
        // Last prayer row
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX);
        row.createCell(row.getLastCellNum()).setCellValue("ፀሎት");
        row.getCell(row.getLastCellNum() - 1).setCellStyle(getCellStyle
                (false, false, true, false, false));
    }

    void makeExcel() {
        insertPageTitle();
        insertHeaderSection();
        for (Meeting meeting : contentParser.getMeetings()) {
            switch (meeting.getKind()) {
                case Meeting.TREASURES:
                    insertTreasuresParts((Treasures) meeting);
                    break;
                case Meeting.IMPROVE_IN_MINISTRY:
                    insertMinistryParts((ImproveInMinistry) meeting);
                    break;
                case Meeting.LIVING_AS_CHRISTIANS:
                    insertChristianLifeParts((LivingAsChristians) meeting);
                    break;
                default:
                    break;
            }
        }
        fillFooterSection();
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
        for (int i = BEGIN_CELL_INDEX; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
