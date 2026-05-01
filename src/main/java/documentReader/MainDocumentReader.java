package documentReader;
import documentReader.exceptions.UnsupportedFileException;
import java.io.File;
import java.util.List;

public class MainDocumentReader {
    public static String getDocumentText(File file, List<DocumentReader> readers) throws UnsupportedFileException {
        String fileExtension = getFileExtension(file);
        for (DocumentReader reader : readers) {
            if (reader.getSupportedExtension().equalsIgnoreCase(fileExtension)) {
                return reader.getDocumentText(file);
            }
        }
        throw new UnsupportedFileException("Reader for given file format not available");
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
}