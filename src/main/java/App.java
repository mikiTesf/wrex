import java.io.File;

class App {

    public static void main(String[] args) {

        File XHTML_FILE_1 = new File(".content/mwb_AM_201904/202019128.xhtml");
        ExcelFileGenerator generator = new ExcelFileGenerator(XHTML_FILE_1);
        generator.makeExcel();
    }
}
