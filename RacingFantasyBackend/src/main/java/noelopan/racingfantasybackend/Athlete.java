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

    // Crucial: TFRRS URLs have unique IDs for athletes (e.g., 7402534)
    // We should eventually scrape this to prevent mixing up two guys named "Chris Johnson"
    private String tfrrsId;

    // An athlete can have many performances
    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL)
    private List<Performance> performances;

    // Getters and Setters...
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
}