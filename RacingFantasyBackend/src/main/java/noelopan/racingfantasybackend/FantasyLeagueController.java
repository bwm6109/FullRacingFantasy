package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "http://localhost:3000")
public class FantasyLeagueController {

    @Autowired
    private FantasyLeagueRepository leagueRepository;

    @Autowired
    private FantasyTeamRepository teamRepository;

    /**
     * POST /api/leagues
     * Creates a new league with a join code.
     * Expects JSON: { "leagueName": "Liberty League Elites", "joinCode": "FAST2026" }
     */
    @PostMapping
    public FantasyLeague createLeague(@RequestBody FantasyLeague league) {
        return leagueRepository.save(league);
    }

    /**
     * GET /api/leagues/{id}
     * Fetches a league and displays all the teams inside it.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FantasyLeague> getLeague(@PathVariable Long id) {
        return leagueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/leagues/{leagueId}/join?teamId=1&joinCode=FAST2026
     * Allows an existing team to join a league if the code matches.
     */
    @PostMapping("/{leagueId}/join")
    public ResponseEntity<String> joinLeague(
            @PathVariable Long leagueId,
            @RequestParam Long teamId,
            @RequestParam String joinCode) {

        Optional<FantasyLeague> leagueOpt = leagueRepository.findById(leagueId);
        Optional<FantasyTeam> teamOpt = teamRepository.findById(teamId);

        // 1. Verify both exist
        if (leagueOpt.isEmpty() || teamOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: League or Team not found.");
        }

        FantasyLeague league = leagueOpt.get();
        FantasyTeam team = teamOpt.get();

        // 2. Verify the join code is correct
        if (!league.getJoinCode().equals(joinCode)) {
            return ResponseEntity.badRequest().body("Error: Incorrect join code.");
        }

        // 3. Verify the team isn't already in a league
        if (team.getLeague() != null) {
            return ResponseEntity.badRequest().body("Error: This team is already in a league.");
        }

        // 4. Assign the team to the league and save
        team.setLeague(league);
        teamRepository.save(team);

        return ResponseEntity.ok("Success! " + team.getTeamName() + " has joined " + league.getLeagueName());
    }

    /**
     * GET /api/leagues/{leagueId}/leaderboard?week=1
     * Calculates the score for all teams in a league and returns them ranked 1st to last.
     */
    @GetMapping("/{leagueId}/leaderboard")
    public ResponseEntity<?> getLeagueLeaderboard(
            @PathVariable Long leagueId,
            @RequestParam Integer week) {

        Optional<FantasyLeague> leagueOpt = leagueRepository.findById(leagueId);

        if (leagueOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FantasyLeague league = leagueOpt.get();
        List<TeamScoreDTO> leaderboard = new ArrayList<>();

        // Loop through every team in the league
        for (FantasyTeam team : league.getTeams()) {
            double totalScore = 0.0;

            // Loop through their roster
            for (Athlete athlete : team.getRoster()) {
                // Loop through the athlete's performances
                for (Performance perf : athlete.getPerformances()) {
                    if (perf.getWeekNumber() != null && perf.getWeekNumber().equals(week)) {
                        totalScore += perf.getFantasyPoints();
                    }
                }
            }

            // Round to 2 decimal places
            totalScore = Math.round(totalScore * 100.0) / 100.0;

            // Safely get the owner's name (fallback if team has no owner)
            String ownerName = (team.getOwner() != null) ? team.getOwner().getUsername() : "Unknown Owner";

            // Create the lightweight DTO and add it to our list
            leaderboard.add(new TeamScoreDTO(team.getTeamName(), ownerName, totalScore));
        }

        // Sort the leaderboard list from highest score to lowest score
        leaderboard.sort((team1, team2) -> Double.compare(team2.getScore(), team1.getScore()));

        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /api/leagues
     * Returns a list of all fantasy leagues.
     */
    @GetMapping
    public List<FantasyLeague> getAllLeagues() {
        return leagueRepository.findAll();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllLeagues() {
        leagueRepository.deleteAll();
        return ResponseEntity.ok("Successfully deleted all leagues");
    }
}