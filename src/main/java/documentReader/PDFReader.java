package documentReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

/**
 * Reads text content from PDF files using Apache PDFBox.
 */
public class PDFReader implements DocumentReader {

    @Override
    public String getDocumentText(File file) {
        String text = "";
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setSortByPosition(true);
                text = textStripper.getText(document).trim();
            }
        } catch (Exception e) {
            System.err.println("Error reading PDF: " + file.getName());
            e.printStackTrace();
        }
        return text;
    }

    @Override
    public String getSupportedExtension() {
        return "pdf";
    }
}
