import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

class App {
    public static void main(String[] args) {
        File[] EPUBFiles;
        EPUBContentExtractor extractor = new EPUBContentExtractor();
        EPUBFiles = new File[3];
        EPUBFiles[0] = new File("sample_mwb/mwb_AM_201812.epub");
        EPUBFiles[1] = new File("sample_mwb/mwb_AM_201904.epub");
        EPUBFiles[2] = new File("sample_mwb/mwb_AM_201905.epub");

        try {
            extractor.unzip(EPUBFiles, new File(".content/"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
