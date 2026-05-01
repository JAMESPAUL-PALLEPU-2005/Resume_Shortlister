package profileDataExtractor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperienceExtractor implements DataExtractor {

    @Override
    public String getDataType() {
        return "Experience";
    }

    @Override
    public String getData(String documentText) {
        String word = "years";
        ArrayList<ExpressionInfo> wordIndexes = getIndexesOf(word, documentText);
        String patternToExtractDigits = "\\d+\\.?\\d*";
        ArrayList<ExpressionInfo> numberIndexes = getIndexesOf(patternToExtractDigits, documentText);
        return getMinimumDistance(wordIndexes, numberIndexes);
    }

    private String getMinimumDistance(ArrayList<ExpressionInfo> wordIndexes, ArrayList<ExpressionInfo> numberIndexes) {
        int minimumDistance = Integer.MAX_VALUE;
        ExpressionInfo experienceYear = null;
        for (ExpressionInfo numberInfo : numberIndexes) {
            for (ExpressionInfo wordInfo : wordIndexes) {
                int distance1 = Math.abs(numberInfo.getStart() - wordInfo.getEnd());
                int distance2 = Math.abs(numberInfo.getEnd() - wordInfo.getStart());
                int minimum = Math.min(distance1, distance2);
                if (minimum < minimumDistance) {
                    minimumDistance = minimum;
                    experienceYear = numberInfo;
                }
            }
        }
        if (experienceYear != null) {
            return experienceYear.getMatchPattern();
        } else return "Unable to retrieve experience";
    }

    private ArrayList<ExpressionInfo> getIndexesOf(String expression, String text) {
        ArrayList<ExpressionInfo> indexInfo = new ArrayList<>();
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            indexInfo.add(new ExpressionInfo(matcher.start(), matcher.end(), matcher.group()));
        }
        return indexInfo;
    }

    private static class ExpressionInfo {
        private int start;
        private int end;
        private String matchPattern;

        ExpressionInfo(int start, int end, String matchPattern) {
            this.start = start;
            this.end = end;
            this.matchPattern = matchPattern;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }

        String getMatchPattern() {
            return matchPattern;
        }
    }
}