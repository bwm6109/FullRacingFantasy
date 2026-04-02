package noelopan.racingfantasybackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FantasyTeamRepository extends JpaRepository<FantasyTeam, Long> {
    Optional<FantasyTeam> findById(Long Id);
}