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

	// 특정 유저의 특정 시간 이후 시작된 모든 게임 수 조회
	int countByRiotUserIdAndStartTimeAfter(Long riotUserId, LocalDateTime startTime);

	// 특정 유저의 특정 기간 동안 시작된 게임 수 조회 (연속 게임 일수 계산)
	int countByRiotUserIdAndStartTimeBetween(Long riotUserId, LocalDateTime startTime, LocalDateTime endTime);

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
