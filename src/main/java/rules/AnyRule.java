package rules;

import java.util.ArrayList;

public class AnyRule implements Rule {
    private ArrayList<Rule> rules;

    public AnyRule(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    public boolean interpret(String documentText) {
        for (Rule rule : rules) {
            if (rule.interpret(documentText)) {
                return true;
            }
        }
        return false;
    }
}
