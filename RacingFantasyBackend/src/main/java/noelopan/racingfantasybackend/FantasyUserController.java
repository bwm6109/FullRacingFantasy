package noelopan.racingfantasybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class FantasyUserController {

    @Autowired
    private FantasyUserRepository userRepository;

    /**
     * POST /api/users
     * Creates a new user account.
     * Expects JSON: { "username": "TrackNerd99", "email": "nerd@example.com" }
     */
    @PostMapping
    public FantasyUser createUser(@RequestBody FantasyUser user) {
        return userRepository.save(user);
    }

    /**
     * GET /api/users
     * Fetches all registered users.
     */
    @GetMapping
    public List<FantasyUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * GET /api/users/{id}
     * Fetches a specific user (and their teams) by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FantasyUser> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/users/{id}
     * Deletes a user account.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Successfully deleted user with ID: " + id);
    }
}