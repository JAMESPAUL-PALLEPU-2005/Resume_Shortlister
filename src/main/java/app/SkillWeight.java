package app;

/**
 * Represents a single skill with its associated weight for scoring.
 * Example: SkillWeight("Java", 30) means Java is worth 30 points.
 */
public class SkillWeight {
    private final String skillName;
    private final int weight;

    public SkillWeight(String skillName, int weight) {
        this.skillName = skillName;
        this.weight = weight;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return skillName + " (weight: " + weight + ")";
    }
}
