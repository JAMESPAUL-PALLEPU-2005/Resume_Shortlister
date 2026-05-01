package documentReader;

import java.io.File;

public interface DocumentReader {
    String getDocumentText(File file);
    String getSupportedExtension();
}
