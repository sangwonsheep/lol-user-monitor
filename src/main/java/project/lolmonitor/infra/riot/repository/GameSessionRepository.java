package project.lolmonitor.infra.riot.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.common.enums.GameStatus;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

	Optional<GameSession> findByRiotUserPuuidAndGameStatus(
		String puuid, GameStatus gameStatus);

	int countByRiotUserIdAndGameStatusAndEndTimeAfter(Long riotUserId, GameStatus gameStatus, LocalDateTime since);

	int countByRiotUserId(Long riotUserId);

	boolean existsByGameId(Long gameId);
}
