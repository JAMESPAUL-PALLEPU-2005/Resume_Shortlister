package rules;

public class ContainsKeyword implements Rule {
    private String word;

    public ContainsKeyword(String word) {
        this.word = word;
    }

    public boolean interpret(String documentText) {
        return documentText.toLowerCase().contains(word.toLowerCase());
    }
}
