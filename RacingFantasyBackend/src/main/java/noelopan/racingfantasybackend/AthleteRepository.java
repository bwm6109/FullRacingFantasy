package noelopan.racingfantasybackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    // This custom method lets the scraper look up an athlete by name and school
    Optional<Athlete> findByNameIgnoreCaseAndSchoolIgnoreCase(String name, String school);
}