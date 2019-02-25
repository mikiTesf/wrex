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
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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

    ExcelFileGenerator() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        boldFont = new XSSFFont();
        boldFont.setBold(true);
        contentParser = new ContentParser();
    }

    private void fillMarginalTexts() {
        // data population starts from the 3rd row
        Row row = sheet.createRow(2);

        formattedText = new XSSFRichTextString();
        formattedText.setString("ክርስቲያናዊ ህይወታችንና አገልግሎታችን");
        formattedText.applyFont(boldFont);
        // set the header of the page
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (false, false, false, true, false));
        //sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 9));
    }

    private void fillHeaderSection() {
        // 4th (index -> 3) row is free
        // 5th row has "week span" in it
        Row row = sheet.createRow(sheet.getLastRowNum() + 2);
        formattedText.setString(contentParser.getWeekSpan());
        boldFont.setBold(false);
        formattedText.applyFont(boldFont);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        // 6th row has the chairman's name
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        formattedText.setString("ሊቀመንበር");
        boldFont.setBold(true);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        // 7th row has the name of the brother who does the opening prayer
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        formattedText.setString("የመክፈቻ ፀሎት");
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
    }

    private void fillTreasuresSection(Treasures treasures) {
        // 8th row has the title of the "Treasures" section
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), 0, 3));
        formattedText.setString("ከአምላክ ቃል የሚገኝ ውድ ሀብት");
        formattedText.applyFont(boldFont); // unchanged since last made bold
        // 10 minute talk
        row.createCell(BEGIN_CELL_INDEX).setCellValue(formattedText);
        row.getCell(BEGIN_CELL_INDEX).setCellStyle(getCellStyle
                (true, true, false, false, false));
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), 1, 2));
        row.createCell(BEGIN_CELL_INDEX).setCellValue(treasures.get10MinuteTalk());
        // digging for  spiritual gems
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX);
        sheet.addMergedRegion(new CellRangeAddress
                (row.getRowNum(), row.getRowNum(), 1, 2));
        row.getCell(BEGIN_CELL_INDEX).setCellValue(treasures.getDiggingForGems());
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        // Bible reading
        insertHallDivisionIndicators(row);
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX).setCellValue(treasures.getBibleReading());
    }

    private void fillMinistrySection(ImproveInMinistry improveInMinistry) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX).setCellValue("በአገልግሎት ውጤታማ ለመሆን ተጣጣር");
        insertHallDivisionIndicators(row);
        // the number of parts is not fixed for all months hence the for loop
        for (String part : improveInMinistry.getParts()) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(BEGIN_CELL_INDEX).setCellValue(part);
        }
    }

    private void insertHallDivisionIndicators(Row row) {
        row.createCell(row.getLastCellNum()).setCellValue("በዋናው አዳራሽ");
        row.getCell(2).setCellStyle(getCellStyle
                (true, false, false, true, true));
        row.createCell(row.getLastCellNum()).setCellValue("በሁለተኛው አዳራሽ");
        row.getCell(3).setCellStyle(getCellStyle
                (true, false, false, true, true));
    }

    private void fillChristianLifeSection(LivingAsChristians livingAsChristians) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(BEGIN_CELL_INDEX).setCellValue("ክርስቲያናዊ ህይወት");
        // the number of parts is not fixed for all months hence the for loop
        for (String part : livingAsChristians.getParts()) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(BEGIN_CELL_INDEX).setCellValue(part);
            sheet.addMergedRegion(new CellRangeAddress
                    (row.getRowNum(), row.getRowNum(), 1, 2));
        }
    }

    private void fillFooterSection() {
        // Congregation Bible study reader row
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(row.getLastCellNum()).setCellValue("አንባቢ");
        // Last prayer row
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(row.getLastCellNum()).setCellValue("ፀሎት");
    }

    void makeExcel(File XHTMLFile) throws IOException {
        for (Meeting meeting : contentParser.parse(XHTMLFile)) {
            fillMarginalTexts();
            fillHeaderSection();
            switch (meeting.getKind()) {
                case Meeting.TREASURES:
                    fillTreasuresSection((Treasures) meeting);
                    break;
                case Meeting.IMPROVE_IN_MINISTRY:
                    fillMinistrySection((ImproveInMinistry) meeting);
                    break;
                case Meeting.LIVING_AS_CHRISTIANS:
                    fillChristianLifeSection((LivingAsChristians) meeting);
                    break;
                default:
                    break;
            }
            fillFooterSection();
        }
        // finalize page setup
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        // write the excel doc on disk
        try {
            FileOutputStream out = new FileOutputStream(new File("cool.xlsx"));
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

        XSSFFont smallFont = workbook.createFont();
        smallFont.setFontHeight(smallerFont ? 18 : 22);
        cellStyle.setFont(smallFont);

        return cellStyle;
    }
}
