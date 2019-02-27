import com.excel.ExcelFileGenerator;

import java.io.File;

class App {

    public static void main(String[] args) {

        ExcelFileGenerator generator = new ExcelFileGenerator
                (new File(".content/mwb_AM_201904/"));
        generator.makeExcel();
    }
}
