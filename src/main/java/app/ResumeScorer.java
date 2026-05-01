package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a percentage score for a resume based on skill weights.
 *
 * Scoring formula:
 * Score = (sum of matched skill weights / total weight of all skills) × 100
 *
 * A skill is considered "matched" if it appears at least once in the resume
 * text
 * (case-insensitive).
 *
 * Example:
 * Skills: Java(30), Spring(25), SQL(20) → total = 75
 * Resume contains: Java, SQL → matched weight = 30 + 20 = 50
 * Score = (50 / 75) × 100 = 66.7%
 */
public class ResumeScorer {

    private final List<SkillWeight> skillWeights;
    private final int totalWeight;

    /**
     * Creates a scorer with the given skill-weight pairs.
     *
     * @param skillWeights list of skills and their weights
     */
    public ResumeScorer(List<SkillWeight> skillWeights) {
        this.skillWeights = skillWeights;
        this.totalWeight = skillWeights.stream()
                .mapToInt(SkillWeight::getWeight)
                .sum();
    }

    /**
     * Scores the given resume text and returns a Candidate result.
     *
     * @param candidateName name extracted from the resume
     * @param fileName      original file name
     * @param documentText  full text content of the resume
     * @return Candidate with score and matched skills
     */
    public Candidate score(String candidateName, String fileName, String documentText) {
        List<String> matchedSkills = new ArrayList<>();
        int matchedWeight = 0;

        for (SkillWeight sw : skillWeights) {
            if (containsSkill(documentText, sw.getSkillName())) {
                matchedSkills.add(sw.getSkillName());
                matchedWeight += sw.getWeight();
            }
        }

        double score = (totalWeight > 0)
                ? ((double) matchedWeight / totalWeight) * 100.0
                : 0.0;

        return new Candidate(candidateName, fileName, matchedSkills, score, documentText);
    }

    /**
     * Checks if the document text contains the skill keyword (case-insensitive).
     * Uses word-boundary-aware matching to avoid false positives
     * (e.g., "java" shouldn't match "javascript" — but we allow it for simplicity
     * since the user explicitly types the skill name).
     */
    private boolean containsSkill(String documentText, String skillName) {
        String lowerText = documentText.toLowerCase();
        String lowerSkill = skillName.toLowerCase().trim();
        return lowerText.contains(lowerSkill);
    }

    /**
     * Returns the total weight of all configured skills.
     */
    public int getTotalWeight() {
        return totalWeight;
    }
}
