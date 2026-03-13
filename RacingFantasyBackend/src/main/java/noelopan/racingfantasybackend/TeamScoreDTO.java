package noelopan.racingfantasybackend;

public class TeamScoreDTO {

    private String teamName;
    private String ownerName;
    private double score;

    public TeamScoreDTO(String teamName, String ownerName, double score) {
        this.teamName = teamName;
        this.ownerName = ownerName;
        this.score = score;
    }

    // Getters
    public String getTeamName() { return teamName; }
    public String getOwnerName() { return ownerName; }
    public double getScore() { return score; }

    // Setters
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setScore(double score) { this.score = score; }
}