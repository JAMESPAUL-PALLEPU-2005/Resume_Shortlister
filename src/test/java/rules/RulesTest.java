package rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * Unit tests for the rule engine (MinimumWordCount, ContainsKeyword, AllRules, AnyRule).
 */
public class RulesTest {

    @Test
    public void testMinimumWordCount_Found() {
        Rule rule = new MinimumWordCount("java", 2);
        String doc = "I know Java and I use Java every day.";
        Assert.assertTrue(rule.interpret(doc), "Should find 'java' at least 2 times (case-insensitive)");
    }

    @Test
    public void testMinimumWordCount_NotEnough() {
        Rule rule = new MinimumWordCount("python", 3);
        String doc = "I know Python well.";
        Assert.assertFalse(rule.interpret(doc), "Should NOT find 'python' 3 times");
    }

    @Test
    public void testContainsKeyword_Found() {
        Rule rule = new ContainsKeyword("C++");
        String doc = "Experienced in C++ and Java.";
        Assert.assertTrue(rule.interpret(doc));
    }

    @Test
    public void testContainsKeyword_NotFound() {
        Rule rule = new ContainsKeyword("Rust");
        String doc = "Experienced in C++ and Java.";
        Assert.assertFalse(rule.interpret(doc));
    }

    @Test
    public void testAllRules_AllPass() {
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(new ContainsKeyword("java"));
        rules.add(new ContainsKeyword("spring"));
        Rule allRules = new AllRules(rules);
        Assert.assertTrue(allRules.interpret("I know Java and Spring Boot."));
    }

    @Test
    public void testAllRules_OneFails() {
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(new ContainsKeyword("java"));
        rules.add(new ContainsKeyword("spring"));
        Rule allRules = new AllRules(rules);
        Assert.assertFalse(allRules.interpret("I know Java but not the other framework."));
    }

    @Test
    public void testAnyRule_OneMatches() {
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(new ContainsKeyword("python"));
        rules.add(new ContainsKeyword("java"));
        Rule anyRule = new AnyRule(rules);
        Assert.assertTrue(anyRule.interpret("Skilled in Java only."));
    }

    @Test
    public void testAnyRule_NoneMatch() {
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(new ContainsKeyword("python"));
        rules.add(new ContainsKeyword("ruby"));
        Rule anyRule = new AnyRule(rules);
        Assert.assertFalse(anyRule.interpret("I only know Java."));
    }
}
