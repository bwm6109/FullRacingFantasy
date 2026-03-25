package noelopan.racingfantasybackend;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "performances")
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    // We replaced the String athleteName and school with this relational object
    @ManyToOne
    @JoinColumn(name = "athlete_id")
    @JsonIgnoreProperties("performances")
    private Athlete athlete;
    private String eventName;
    private String place;
    private String year;
    private String school;
    private String displayMark;
    private double decimalMark;
    private Integer points;
    private double pointsMultiplier;
    private double fantasyPoints;
    private Integer weekNumber;
    private String athleteName;

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setPlace(String place) {
        this.place = place;
        this.pointsMultiplier = 1.0; // Default fallback

        try {
            // Removes letters so "3T" becomes "3". Ignores completely blank strings.
            String numericPlace = place.replaceAll("[^0-9]", "");

            if (!numericPlace.isEmpty()) {
                int placeInt = Integer.parseInt(numericPlace);
                switch (placeInt) {
                    case 1: pointsMultiplier = 1.2; break;
                    case 2: pointsMultiplier = 1.16; break;
                    case 3: pointsMultiplier = 1.12; break;
                    case 4: pointsMultiplier = 1.10; break;
                    case 5: pointsMultiplier = 1.08; break;
                    case 6: pointsMultiplier = 1.06; break;
                    case 7: pointsMultiplier = 1.04; break;
                    case 8: pointsMultiplier = 1.02; break;
                    default: pointsMultiplier = 1.0; break;
                }
            }
        } catch (Exception e) {
            // If it's "DNF" or "NH", it safely falls back to a 1.0 multiplier
            this.pointsMultiplier = 1.0;
        }
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
        this.athleteName = athlete.getName();
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public void setDisplayMark(String displayMark) {
        this.displayMark = displayMark;
    }

    public void setDecimalMark(double decimalMark) {
        this.decimalMark = decimalMark;
    }

    public void setPoints(Integer points) {
        this.points = points;
        this.fantasyPoints = Math.round((pointsMultiplier * points * 0.02) / .01) * .01;
    }

    public void setWeekNumber(Integer weekNumber){
        this.weekNumber = weekNumber;
    }

    public long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPlace() {
        return place;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public String getYear() {
        return year;
    }

    public String getSchool() {
        return school;
    }

    public String getDisplayMark() {
        return displayMark;
    }

    public double getDecimalMark() {
        return decimalMark;
    }

    public Integer getPoints() {
        return points;
    }

    public double getFantasyPoints() {
        return fantasyPoints;
    }

    public Integer getWeekNumber(){
        return weekNumber;
    }

    public String getAthleteName() {
        return athleteName;
    }
}
