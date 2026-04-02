package noelopan.racingfantasybackend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Optional<Performance> findByAthleteAndEventNameIgnoreCaseAndWeekNumber(Athlete athlete, String eventName, Integer weekNumber);
    List<Performance> findAllByWeekNumber(Integer weekNumber);
    void deleteAllByWeekNumber(Integer weekNumber);
}
