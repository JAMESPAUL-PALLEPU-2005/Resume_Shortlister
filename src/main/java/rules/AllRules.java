package rules;

import java.util.ArrayList;

public class AllRules implements Rule {
    private ArrayList<Rule> rules;

    public AllRules(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    public boolean interpret(String documentText) {
        for (Rule rule : rules) {
            if (!rule.interpret(documentText)) {
                return false;
            }
        }
        return true;
    }
}
