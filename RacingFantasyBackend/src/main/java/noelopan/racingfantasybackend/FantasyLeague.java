package noelopan.racingfantasybackend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "fantasy_leagues")
public class FantasyLeague {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leagueName;
    private String joinCode;

    // A league contains many teams
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("league") // Prevents infinite JSON loops
    private List<FantasyTeam> teams = new ArrayList<>();

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public List<FantasyTeam> getTeams() { return teams; }
    public void setTeams(List<FantasyTeam> teams) { this.teams = teams; }
}