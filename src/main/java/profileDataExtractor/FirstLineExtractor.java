package profileDataExtractor;

public class FirstLineExtractor implements DataExtractor {
    private static final String NOT_FOUND = "Unable to find data.";

    @Override
    public String getDataType() {
        return "Resume Head";
    }

    @Override
    public String getData(String documentText) {
        if (documentText == null || documentText.trim().isEmpty()) {
            return NOT_FOUND;
        }

        String[] lines = documentText.split("\\R");
        for (String line : lines) {
            String normalized = normalize(line);
            if (looksLikeCandidateName(normalized)) {
                return normalized;
            }
        }

        return NOT_FOUND;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private boolean looksLikeCandidateName(String line) {
        if (line.isEmpty() || isPlaceholderLine(line)) {
            return false;
        }

        String candidate = stripNameLabel(line);
        if (candidate.isEmpty() || isPlaceholderLine(candidate)) {
            return false;
        }

        if (candidate.length() > 60 || candidate.contains("@")) {
            return false;
        }

        if (candidate.matches(".*\\d{3,}.*")) {
            return false;
        }

        if (candidate.matches("(?i)^(resume|curriculum vitae|cv|profile|summary|objective|experience|education|skills|projects|contact|phone|email|address)\\b.*")) {
            return false;
        }

        if (candidate.matches("(?i).*(engineer|developer|architect|analyst|consultant|manager|intern|specialist|administrator|scientist|designer|tester|devops|student|fresher|lead)\\b.*")) {
            return false;
        }

        return candidate.matches("(?iu)^[\\p{L}][\\p{L}.'’\\-]*(\\s+[\\p{L}][\\p{L}.'’\\-]*){0,4}$");
    }

    private String stripNameLabel(String line) {
        return line.replaceFirst("(?i)^(candidate\\s+name|name)\\s*[:\\-]\\s*", "").trim();
    }

    private boolean isPlaceholderLine(String line) {
        return line.matches("(?i)^\\[?\\s*type\\s+text\\s*\\]?(\\s+\\[?\\s*type\\s+text\\s*\\]?)*$");
    }
}
