package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
@CrossOrigin(origins = "http://localhost:3000")
public class AthleteController {

    @Autowired
    private AthleteRepository athleteRepository;

    /**
     * DELETE /api/athletes
     * Wipes ALL athletes from the database.
     * WARNING: Because of database relations, make sure you delete all
     * Performances and Teams FIRST, otherwise PostgreSQL will block this!
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllAthletes() {
        athleteRepository.deleteAll();
        return ResponseEntity.ok("All athletes have been wiped clean.");
    }

    /**
     * GET /api/athletes
     * Returns a list of all athletes in the database.
     */
    @GetMapping
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }
}