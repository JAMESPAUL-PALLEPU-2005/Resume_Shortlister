package profileDataExtractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for data extractors that use regex patterns to find data within document text.
 */
abstract class PatternDataExtractor implements DataExtractor {

    /**
     * Returns the regex pattern used to extract data.
     */
    abstract String getPattern();

    @Override
    public String getData(String documentText) {
        Pattern pattern = Pattern.compile(getPattern());
        Matcher matcher = pattern.matcher(documentText);

        StringBuilder data = new StringBuilder();
        boolean found = false;
        while (matcher.find()) {
            if (found) data.append(" ");
            data.append(matcher.group());
            found = true;
        }

        return found ? data.toString() : "Unable to find data.";
    }
}
