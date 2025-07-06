package project.lolmonitor.infra.riot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.infra.riot.entity.GameStatus;
import project.lolmonitor.infra.riot.entity.RiotUser;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

	Optional<GameSession> findByRiotUserAndGameIdAndGameStatus(
		RiotUser riotUser, Long gameId, GameStatus gameStatus);

	boolean existsByGameId(Long gameId);
}
