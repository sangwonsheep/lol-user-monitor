package project.lolmonitor.infra.riot.repository;

import java.time.LocalDateTime;
import java.util.List;
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

	/**
	 * 특정 기간 내 완료된 게임 세션 조회
	 */
	List<GameSession> findByGameStatusAndStartTimeBetweenOrderByStartTimeDesc(
		GameStatus gameStatus,
		LocalDateTime startTime,
		LocalDateTime endTime
	);

	/**
	 * 편의 메서드: 완료된 게임만 조회
	 */
	default List<GameSession> findCompletedGamesByPeriod(LocalDateTime startTime, LocalDateTime endTime) {
		return findByGameStatusAndStartTimeBetweenOrderByStartTimeDesc(
			GameStatus.COMPLETED, startTime, endTime);
	}
}
