package noelopan.racingfantasybackend;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "athletes")
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String school;

    private String tfrrsId;
    private String bestEvent;
    private String bestEventMark;
    private Integer bestEventPoints;
    private double bestEventFantasyPoints;
    private String tfrrsUrl;
    private int leaguePoints = 0;

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL)
    private List<Performance> performances;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getTfrrsId() { return tfrrsId; }
    public void setTfrrsId(String tfrrsId) { this.tfrrsId = tfrrsId; }

    public List<Performance> getPerformances() { return performances; }
    public void setPerformances(List<Performance> performances) { this.performances = performances; }

    public String getBestEvent() { return bestEvent; }
    public void setBestEvent(String bestEvent) { this.bestEvent = bestEvent; }

    public String getBestEventMark() { return bestEventMark; }
    public void setBestEventMark(String bestEventMark) { this.bestEventMark = bestEventMark; }

    public Integer getBestEventPoints() { return bestEventPoints; }
    public void setBestEventPoints(Integer bestEventPoints) { this.bestEventPoints = bestEventPoints; }

    public double getBestEventFantasyPoints() { return bestEventFantasyPoints; }
    public void setBestEventFantasyPoints(double bestEventFantasyPoints) {
        this.bestEventFantasyPoints = bestEventFantasyPoints;
    }

    public String getTfrrsUrl() { return tfrrsUrl; }
    public void setTfrrsUrl(String tfrrsUrl) { this.tfrrsUrl = tfrrsUrl; }

    public void trySetBestEventAndPoints(String event, String mark, int points) {
        if (bestEventPoints == null || bestEventPoints < points) {
            this.bestEvent = event;
            this.bestEventMark = mark;
            this.bestEventPoints = points;
            this.bestEventFantasyPoints = points / 50.0;
        }
    }
    public int getLeaguePoints(){
        return leaguePoints;
    }
    public void incrementLeaguePoints(int points){
        leaguePoints += points;
    }
}