package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single candidate (resume) with scoring information.
 * Stores the candidate name, matched skills, and calculated score.
 */
public class Candidate implements Comparable<Candidate> {
    private final String name;
    private final String fileName;
    private final List<String> matchedSkills;
    private final double score;
    private final String resumeText;

    public Candidate(String name, String fileName, List<String> matchedSkills, double score) {
        this(name, fileName, matchedSkills, score, "");
    }

    public Candidate(String name, String fileName, List<String> matchedSkills, double score, String resumeText) {
        this.name = name;
        this.fileName = fileName;
        this.matchedSkills = new ArrayList<>(matchedSkills);
        this.score = score;
        this.resumeText = resumeText;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public double getScore() {
        return score;
    }

    public String getResumeText() {
        return resumeText;
    }

    /**
     * Natural ordering: highest score first (descending).
     */
    @Override
    public int compareTo(Candidate other) {
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("%-30s | Score: %5.1f%% | Matched: %s",
                name, score, String.join(", ", matchedSkills));
    }
}
