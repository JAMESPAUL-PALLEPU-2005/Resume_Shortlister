package profileDataExtractor;

public interface DataExtractor {
    String getDataType();
    String getData(String documentText);
}
