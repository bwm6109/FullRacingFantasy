package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/athletes")
@CrossOrigin(origins = "http://localhost:3000")
public class AthleteController {

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private TfrrsScraperService scraperService;

    /**
     * DELETE /api/athletes
     * Wipes ALL athletes from the database.
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllAthletes() {
        athleteRepository.deleteAll();
        return ResponseEntity.ok("All athletes have been wiped clean.");
    }

    @DeleteMapping("/{tfrrsId}")
    public ResponseEntity<Athlete> deleteAthlete(@PathVariable long tfrrsId) {
            Optional<Athlete> deletingAthlete = athleteRepository.findById(tfrrsId);
            if(deletingAthlete.isEmpty()) {
                System.out.println("athlete is empty");
                return ResponseEntity.notFound().build();
            }
            Athlete athlete = deletingAthlete.get();
        athleteRepository.delete(athlete);
        return ResponseEntity.ok(athlete);
    }

    /**
     * GET /api/athletes
     * Returns a list of all athletes in the database.
     */
    @GetMapping
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    /**
     * POST /api/athletes/import-rosters
     * Imports athletes from multiple TFRRS team roster URLs.
     *
     * Example JSON:
     * {
     *   "gender": "mens",
     *   "rosterUrls": [
     *     "https://www.tfrrs.org/teams/tf/NY_college_m_RIT.html",
     *     "https://www.tfrrs.org/teams/tf/NY_college_m_St_Lawrence.html"
     *   ]
     * }
     */
    @PostMapping("/import-rosters")
    public ResponseEntity<String> importRosters(@RequestBody Map<String, Object> requestData) {
        Object urlsObj = requestData.get("rosterUrls");
        Object genderObj = requestData.get("gender");

        if (!(urlsObj instanceof List<?> urls) || genderObj == null) {
            return ResponseEntity.badRequest().body("Error: Please provide 'gender' and a list of 'rosterUrls'.");
        }

        String gender = genderObj.toString().trim();
        if (gender.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: gender cannot be blank.");
        }

        int successCount = 0;

        for (Object urlObj : urls) {
            if (urlObj == null) continue;

            String rosterUrl = urlObj.toString().trim();
            if (rosterUrl.isEmpty()) continue;

            scraperService.scrapeRoster(rosterUrl, gender);
            successCount++;
        }

        return ResponseEntity.ok("Successfully imported " + successCount + " roster(s).");
    }
}