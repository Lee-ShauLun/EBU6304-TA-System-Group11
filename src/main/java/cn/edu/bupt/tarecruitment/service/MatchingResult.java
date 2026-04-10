package cn.edu.bupt.tarecruitment.service;

public class MatchingResult {

    private final int score;
    private final String matchedSkills;
    private final String missingSkills;
    private final String explanation;

    public MatchingResult(
            int score, String matchedSkills, String missingSkills, String explanation) {
        this.score = score;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.explanation = explanation;
    }
    public boolean hasMissingSkills() {

        return missingSkills != null && !missingSkills.trim().isEmpty();

    }
    public double getScorePercentage() {
        return score / 100.0;
    }
    public int getScore() {
        return score;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public String getExplanation() {
        return explanation;
    }
}
