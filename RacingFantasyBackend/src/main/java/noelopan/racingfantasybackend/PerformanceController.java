package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // 1. Tells Spring this class handles web requests
@RequestMapping("/api/performances") // 2. The base URL for all endpoints in this class
@CrossOrigin(origins = "http://localhost:3000") // 3. CRUCIAL: Allows your React app to talk to this API
public class PerformanceController {

    @Autowired
    private TfrrsScraperService scraperService;

    @Autowired
    private PerformanceRepository performanceRepository;

    /**
     * GET /api/performances
     * Fetches all performances currently saved in the database.
     */
    @GetMapping
    public List<Performance> getAllPerformances() {
        // Automatically runs a "SELECT * FROM performances" query
        return performanceRepository.findAll();
    }

    /**
     * POST /api/performances/scrape
     * Triggers the scraper to run on a specific TFRRS URL and assigns it to a week.
     * Expects a JSON body like: { "url": "https://www.tfrrs.org/...", "weekNumber": 1 }
     */
    @PostMapping("/scrape")
    public ResponseEntity<String> triggerScrape(@RequestBody Map<String, Object> requestData) {
        String meetUrl = (String) requestData.get("url");

        // Safely extract the week number, handling potential nulls or string conversions
        Integer weekNumber = null;
        if (requestData.get("weekNumber") != null) {
            weekNumber = Integer.parseInt(requestData.get("weekNumber").toString());
        }

        if (meetUrl == null || meetUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Please provide a valid 'url'.");
        }
        if (weekNumber == null) {
            return ResponseEntity.badRequest().body("Error: Please provide a 'weekNumber'.");
        }

        try {
            System.out.println("Scraping week " + weekNumber + " meet: " + meetUrl);
            scraperService.scrapeMeet(meetUrl, weekNumber); // We will update this method next!
            return ResponseEntity.ok("Successfully scraped and scored meet for Week " + weekNumber);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Scraping error: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/performances/{id}
     * Deletes a specific performance by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePerformance(@PathVariable Long id) {
        if (!performanceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        performanceRepository.deleteById(id);
        return ResponseEntity.ok("Successfully deleted performance with ID: " + id);
    }

    /**
     * DELETE /api/performances
     * Wipes ALL performances from the database. Great for a hard reset!
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllPerformances() {
        performanceRepository.deleteAll();
        return ResponseEntity.ok("All performances have been wiped clean.");
    }
}