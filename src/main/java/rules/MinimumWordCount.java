package rules;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinimumWordCount implements Rule {
	private int minimumCount;
	private String word;

	public MinimumWordCount(String word, int count) {
		this.word = word;
		this.minimumCount = count;
	}

	public boolean interpret(String documentText) {
		return getCountOfWordOccurrence(documentText) >= minimumCount;
	}

	private int getCountOfWordOccurrence(String documentText) {
		int count = 0;
		Scanner scanner = new Scanner(documentText);
		String lowerCaseWord = word.toLowerCase();
		Pattern pattern = Pattern.compile(lowerCaseWord);
		while (scanner.hasNextLine()) {
			String nextLine = scanner.nextLine().toLowerCase();
			Matcher matcher = pattern.matcher(nextLine);
			while (matcher.find())
				count++;
		}
		scanner.close();
		return count;
	}
}
