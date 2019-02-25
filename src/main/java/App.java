import java.io.File;
import java.io.IOException;

class App {

    public static void main(String[] args) {

        File XHTML_FILE_1 = new File(".content/mwb_AM_201904/202019128.xhtml");
        ExcelFileGenerator generator = new ExcelFileGenerator();
        try {
            generator.makeExcel(XHTML_FILE_1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
