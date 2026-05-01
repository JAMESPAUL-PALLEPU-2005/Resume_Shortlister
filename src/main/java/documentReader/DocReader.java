package documentReader;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

/**
 * Reads text content from legacy .doc files using Apache POI.
 */
public class DocReader implements DocumentReader {

    @Override
    public String getDocumentText(File file) {
        String text = "";
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor wordExtractor = new WordExtractor(document)) {
            text = wordExtractor.getText();
        } catch (Exception e) {
            System.err.println("Error reading DOC: " + file.getName());
            e.printStackTrace();
        }
        return text;
    }

    @Override
    public String getSupportedExtension() {
        return "doc";
    }
}
