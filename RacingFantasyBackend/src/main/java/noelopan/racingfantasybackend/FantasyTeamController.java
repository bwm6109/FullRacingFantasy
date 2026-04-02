package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:3000")
public class FantasyTeamController {

    @Autowired
    private FantasyTeamRepository teamRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private FantasyUserRepository userRepository;

    /**
     * POST /api/teams?userId=1
     * Creates a new team and assigns it to a specific User.
     */
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestParam Long userId, @RequestBody FantasyTeam team) {

        Optional<FantasyUser> userOpt = userRepository.findById(userId);

        // 1. If the user doesn't exist, return a 400 Bad Request with a String
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: User with ID " + userId + " not found.");
        }

        // 2. If the user DOES exist, assign them to the team and save
        FantasyUser user = userOpt.get();
        team.setOwner(user);

        FantasyTeam savedTeam = teamRepository.save(team);

        // 3. Return a 200 OK with the FantasyTeam object
        return ResponseEntity.ok(savedTeam);
    }

    /**
     * GET /api/teams/{teamId}
     * Returns the team and its entire drafted roster.
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<FantasyTeam> getTeam(@PathVariable Long teamId) {
        return teamRepository.findById(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/teams/{teamId}/draft/{athleteId}
     * Drafts a specific athlete to a specific team.
     */
    @PostMapping("/{teamId}/draft/{athleteId}")
    public ResponseEntity<String> draftAthlete(@PathVariable Long teamId, @PathVariable Long athleteId) {
        Optional<FantasyTeam> teamOpt = teamRepository.findById(teamId);
        Optional<Athlete> athleteOpt = athleteRepository.findById(athleteId);

        // 1. Verify both exist
        if (teamOpt.isEmpty() || athleteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Team or Athlete not found.");
        }

        FantasyTeam team = teamOpt.get();
        Athlete athlete = athleteOpt.get();

        // 2. Prevent drafting the same person twice to the same team
        if (team.getRoster().contains(athlete)) {
            return ResponseEntity.badRequest().body("Error: " + athlete.getName() + " is already on this roster.");
        }

        // 3. Draft the athlete and save
        team.getRoster().add(athlete);
        teamRepository.save(team);

        return ResponseEntity.ok("Success! Drafted " + athlete.getName() + " to " + team.getTeamName());
    }

    /**
     * GET /api/teams/{teamId}/score?week=1
     * Calculates the total fantasy points for a team in a specific week.
     */
    @GetMapping("/{teamId}/score")
    public ResponseEntity<Double> getTeamScoreForWeek(
            @PathVariable Long teamId,
            @RequestParam Integer week) {

        Optional<FantasyTeam> teamOpt = teamRepository.findById(teamId);

        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FantasyTeam team = teamOpt.get();
        double totalTeamScore = 0.0;

        // Loop through every drafted athlete on the roster
        for (Athlete athlete : team.getRoster()) {

            // Loop through all their performances
            for (Performance perf : athlete.getPerformances()) {

                // If the performance happened in the requested week, add it to the total
                if (perf.getWeekNumber() != null && perf.getWeekNumber().equals(week)) {
                    totalTeamScore += perf.getFantasyPoints();
                }
            }
        }

        // Round the final team score to 2 decimal places to keep it clean
        totalTeamScore = Math.round(totalTeamScore * 100.0) / 100.0;

        return ResponseEntity.ok(totalTeamScore);
    }

    /**
     * DELETE /api/teams/{teamId}
     * Deletes an entire fantasy team.
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            return ResponseEntity.notFound().build();
        }
        teamRepository.deleteById(teamId);
        return ResponseEntity.ok("Successfully deleted team with ID: " + teamId);
    }

    /**
     * DELETE /api/teams/{teamId}/roster/{athleteId}
     * Drops a specific athlete from a team's roster.
     */
    @DeleteMapping("/{teamId}/roster/{athleteId}")
    public ResponseEntity<String> dropAthlete(@PathVariable Long teamId, @PathVariable Long athleteId) {
        Optional<FantasyTeam> teamOpt = teamRepository.findById(teamId);
        Optional<Athlete> athleteOpt = athleteRepository.findById(athleteId);

        if (teamOpt.isEmpty() || athleteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Team or Athlete not found.");
        }

        FantasyTeam team = teamOpt.get();
        Athlete athlete = athleteOpt.get();

        if (!team.getRoster().contains(athlete)) {
            return ResponseEntity.badRequest().body("Error: Athlete is not on this team's roster.");
        }

        // Remove the athlete from the list and save
        team.getRoster().remove(athlete);
        teamRepository.save(team);

        return ResponseEntity.ok("Successfully dropped " + athlete.getName() + " from " + team.getTeamName());
    }

    /**
     * GET /api/teams
     * Returns a list of all fantasy teams.
     */
    @GetMapping
    public List<FantasyTeam> getAllTeams() {
        return teamRepository.findAll();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllTeams() {
        teamRepository.deleteAll();
        return ResponseEntity.ok("Successfully deleted all teams");
    }

    @PutMapping("/updateName/{teamId}")
    public ResponseEntity<String> updateTeamName(@PathVariable long teamId, @RequestParam String teamName) {
        Optional<FantasyTeam> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Team not found.");
        }
        FantasyTeam team = teamOpt.get();
        team.setTeamName(teamName);
        teamRepository.save(team);
        return ResponseEntity.ok("Successfully updated team with ID: " + teamId);
    }
}