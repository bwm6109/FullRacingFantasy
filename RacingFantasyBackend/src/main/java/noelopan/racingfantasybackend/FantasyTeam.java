package noelopan.racingfantasybackend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "fantasy_teams")
public class FantasyTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;

    // 1. Which User owns this team?
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("teams")
    private FantasyUser owner;

    // 2. Which League is this team in?
    @ManyToOne
    @JoinColumn(name = "league_id")
    @JsonIgnoreProperties("teams")
    private FantasyLeague league;

    // 3. The Roster: Which real-world Athletes are on this team?
    @ManyToMany
    @JoinTable(
            name = "team_roster",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )
    private List<Athlete> roster = new java.util.ArrayList<>();

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public FantasyUser getOwner() { return owner; }
    public void setOwner(FantasyUser owner) { this.owner = owner; }
    public FantasyLeague getLeague() { return league; }
    public void setLeague(FantasyLeague league) { this.league = league; }
    public List<Athlete> getRoster() { return roster; }
    public void setRoster(List<Athlete> roster) { this.roster = roster; }
}