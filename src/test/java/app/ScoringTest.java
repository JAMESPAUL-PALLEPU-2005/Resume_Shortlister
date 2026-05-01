package app;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the Resume Scoring and Ranking System.
 * Tests Candidate, SkillWeight, and ResumeScorer classes.
 */
public class ScoringTest {

    // ── SkillWeight Tests ──

    @Test
    public void testSkillWeightCreation() {
        SkillWeight sw = new SkillWeight("Java", 30);
        Assert.assertEquals(sw.getSkillName(), "Java");
        Assert.assertEquals(sw.getWeight(), 30);
    }

    // ── Candidate Tests ──

    @Test
    public void testCandidateCreation() {
        Candidate c = new Candidate("John Doe", "john.pdf",
                Arrays.asList("Java", "SQL"), 66.7);
        Assert.assertEquals(c.getName(), "John Doe");
        Assert.assertEquals(c.getFileName(), "john.pdf");
        Assert.assertEquals(c.getMatchedSkills().size(), 2);
        Assert.assertEquals(c.getScore(), 66.7, 0.01);
    }

    @Test
    public void testCandidateSortingDescending() {
        Candidate c1 = new Candidate("Low", "a.pdf", Collections.emptyList(), 30.0);
        Candidate c2 = new Candidate("High", "b.pdf", Collections.emptyList(), 90.0);
        Candidate c3 = new Candidate("Mid", "c.pdf", Collections.emptyList(), 60.0);

        List<Candidate> list = Arrays.asList(c1, c2, c3);
        Collections.sort(list);

        Assert.assertEquals(list.get(0).getName(), "High");
        Assert.assertEquals(list.get(1).getName(), "Mid");
        Assert.assertEquals(list.get(2).getName(), "Low");
    }

    // ── ResumeScorer Tests ──

    @Test
    public void testPerfectScore() {
        List<SkillWeight> weights = Arrays.asList(
                new SkillWeight("Java", 30),
                new SkillWeight("Spring", 25),
                new SkillWeight("SQL", 20));

        ResumeScorer scorer = new ResumeScorer(weights);

        String resume = "I am experienced in Java, Spring Boot, and SQL databases.";
        Candidate result = scorer.score("Alice", "alice.pdf", resume);

        Assert.assertEquals(result.getScore(), 100.0, 0.01);
        Assert.assertEquals(result.getMatchedSkills().size(), 3);
    }

    @Test
    public void testPartialScore() {
        List<SkillWeight> weights = Arrays.asList(
                new SkillWeight("Java", 30),
                new SkillWeight("Spring", 25),
                new SkillWeight("SQL", 20));

        ResumeScorer scorer = new ResumeScorer(weights);

        // Resume has Java and SQL but NOT Spring
        String resume = "Proficient in Java programming and SQL query optimization.";
        Candidate result = scorer.score("Bob", "bob.pdf", resume);

        // Expected: (30 + 20) / 75 * 100 = 66.67%
        Assert.assertEquals(result.getScore(), 66.67, 0.1);
        Assert.assertEquals(result.getMatchedSkills().size(), 2);
        Assert.assertTrue(result.getMatchedSkills().contains("Java"));
        Assert.assertTrue(result.getMatchedSkills().contains("SQL"));
        Assert.assertFalse(result.getMatchedSkills().contains("Spring"));
    }

    @Test
    public void testZeroScore() {
        List<SkillWeight> weights = Arrays.asList(
                new SkillWeight("Java", 30),
                new SkillWeight("Python", 25));

        ResumeScorer scorer = new ResumeScorer(weights);

        String resume = "Expert in Ruby on Rails and Go programming.";
        Candidate result = scorer.score("Charlie", "charlie.pdf", resume);

        Assert.assertEquals(result.getScore(), 0.0, 0.01);
        Assert.assertTrue(result.getMatchedSkills().isEmpty());
    }

    @Test
    public void testCaseInsensitiveMatching() {
        List<SkillWeight> weights = Arrays.asList(
                new SkillWeight("JAVA", 30),
                new SkillWeight("sql", 20));

        ResumeScorer scorer = new ResumeScorer(weights);

        String resume = "Skills: java, SQL Server, PostgreSQL";
        Candidate result = scorer.score("Dave", "dave.pdf", resume);

        Assert.assertEquals(result.getScore(), 100.0, 0.01);
        Assert.assertEquals(result.getMatchedSkills().size(), 2);
    }

    @Test
    public void testEmptySkillWeights() {
        ResumeScorer scorer = new ResumeScorer(Collections.emptyList());

        String resume = "Any resume content here.";
        Candidate result = scorer.score("Eve", "eve.pdf", resume);

        Assert.assertEquals(result.getScore(), 0.0, 0.01);
        Assert.assertTrue(result.getMatchedSkills().isEmpty());
    }

    @Test
    public void testTotalWeight() {
        List<SkillWeight> weights = Arrays.asList(
                new SkillWeight("Java", 30),
                new SkillWeight("Spring", 25),
                new SkillWeight("SQL", 20));

        ResumeScorer scorer = new ResumeScorer(weights);
        Assert.assertEquals(scorer.getTotalWeight(), 75);
    }
}
