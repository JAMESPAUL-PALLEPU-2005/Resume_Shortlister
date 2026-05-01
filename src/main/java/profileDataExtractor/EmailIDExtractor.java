package profileDataExtractor;

public class EmailIDExtractor extends PatternDataExtractor {
    @Override
    String getPattern() {
        return "\\S+@\\S+\\.\\S+";
    }

    @Override
    public String getDataType() {
        return "Email ID";
    }
}
