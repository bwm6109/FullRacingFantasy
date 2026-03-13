package noelopan.racingfantasybackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FantasyLeagueRepository extends JpaRepository<FantasyLeague, Long> {
}