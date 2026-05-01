package profileDataExtractor;

/**
 * Extracts phone/contact numbers from resume text.
 * Supports:
 *   - +<country_code> followed by 10 digits (e.g. +91 9876543210, +1 1234567890)
 *   - Plain 10-digit numbers (e.g. 9876543210)
 *   - Numbers with dashes or spaces (e.g. 987-654-3210, 987 654 3210)
 */
public class ContactExtractor extends PatternDataExtractor {

    @Override
    String getPattern() {
        // Flexible pattern: optional + with 1-3 digit country code, then 10 digits
        // Allows spaces, dashes, and optional parentheses
        return "(\\+?\\d{1,3})?[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{4}";
    }

    @Override
    public String getDataType() {
        return "Contact";
    }
}
