package project.lolmonitor.infra.riot.datahandler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.lolmonitor.client.riot.dto.CurrentGameInfo;
import project.lolmonitor.client.riot.dto.CurrentGameParticipant;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;
import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.common.enums.GameStatus;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.repository.GameSessionRepository;
import project.lolmonitor.infra.riot.repository.RiotUserRepository;

@Component
@RequiredArgsConstructor
public class GameSessionDataHandler {

	// 하루 기준 시간 (08:30)
	private static final LocalTime DAILY_RESET_TIME = LocalTime.of(8, 30);

	private final GameSessionRepository gameSessionRepository;
	private final RiotUserRepository riotUserRepository;

	@Transactional(readOnly = true)
	public GameSession getActiveGameSession(String puuid) {
		return gameSessionRepository.findByRiotUserPuuidAndGameStatus(puuid, GameStatus.IN_PROGRESS)
									.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자이거나 유효하지 않은 puuid : " + puuid));
	}

	@Transactional(readOnly = true)
	public boolean existsGameSession(Long gameId) {
		return gameSessionRepository.existsByGameId(gameId);
	}

	@Transactional(readOnly = true)
	public int countGameSessionsByRiotUser(Long riotUserId) {
		return gameSessionRepository.countByRiotUserId(riotUserId);
	}

	@Transactional(readOnly = true)
	public int countCompletedGamesSince(Long riotUserId, LocalDateTime since) {
		return gameSessionRepository.countByRiotUserIdAndGameStatusAndEndTimeAfter(
			riotUserId, GameStatus.COMPLETED, since);
	}

	@Transactional(readOnly = true)
	public int countTodayAllGames(Long riotUserId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime todayResetTime = now.toLocalDate().atTime(DAILY_RESET_TIME);

		// 현재 시간이 08:30 이전이면 어제 08:30부터 카운트
		LocalDateTime dailyStartTime = now.toLocalTime().isBefore(DAILY_RESET_TIME)
			? todayResetTime.minusDays(1)
			: todayResetTime;

		return gameSessionRepository.countByRiotUserIdAndStartTimeAfter(riotUserId, dailyStartTime);
	}

	/**
	 * 유저의 연속 게임 일수 계산 (08:30 기준)
	 * 하루라도 0판이면 초기화되고, 1판 이상인 날만 카운트
	 */
	@Transactional(readOnly = true)
	public int calculateConsecutiveGameDays(Long riotUserId, LocalDateTime baseTime) {
		// 기준 시간이 08:30 이하이면 어제부터 시작
		LocalDate currentDay = baseTime.toLocalTime().isAfter(DAILY_RESET_TIME)
			? baseTime.toLocalDate()               // 08:30 초과
			: baseTime.toLocalDate().minusDays(1); // 08:30 이하

		int consecutiveDays = 0;

		// 0판인 날이 나올 때까지 계속 체크
		while (true) {
			LocalDateTime dayStart = currentDay.atTime(DAILY_RESET_TIME);
			LocalDateTime dayEnd = currentDay.plusDays(1).atTime(DAILY_RESET_TIME);

			// 해당 일의 게임 수 조회
			int gamesInDay = gameSessionRepository.countByRiotUserIdAndStartTimeBetween(
				riotUserId, dayStart, dayEnd);

			if (gamesInDay > 0) {
				consecutiveDays++;
				currentDay = currentDay.minusDays(1); // 하루 전으로 이동
			} else {
				break; // 0판인 날이 나오면 연속 중단
			}
		}

		return consecutiveDays;
	}

	@Transactional
	public GameSession startGameSession(CurrentGameInfo gameInfo, CurrentGameParticipant participant, String puuid) {
		RiotUser riotUser = riotUserRepository.findByPuuid(puuid)
											  .orElseThrow(() -> new RuntimeException(
												  "존재하지 않는 사용자이거나 유효하지 않은 puuid : " + puuid));

		LocalDateTime gameStartTime = gameInfo.gameStartTime() != null
			? LocalDateTime.ofInstant(Instant.ofEpochMilli(gameInfo.gameStartTime()), ZoneId.systemDefault())
			: LocalDateTime.now();

		GameSession gameSession = GameSession.createGameSession(riotUser, gameInfo.gameId(), gameInfo.gameType(),
			gameInfo.gameMode(), gameInfo.gameLength(), GameStatus.IN_PROGRESS, gameInfo.mapId(), participant.championId(), participant.teamId(),
			gameStartTime);
		return gameSessionRepository.save(gameSession);
	}

	@Transactional
	public void endGameSession(String puuid) {
		GameSession gameSession = getActiveGameSession(puuid);
		gameSession.endGame(LocalDateTime.now());
		gameSessionRepository.save(gameSession);
	}

	@Transactional(readOnly = true)
	public List<DailyUserGameStats> getGameStatistics(LocalDateTime startTime, LocalDateTime endTime) {
		// 모든 모니터링 대상 유저 조회
		List<RiotUser> monitoredUsers = riotUserRepository.findByIsMonitoredTrue();

		// 기간 내 완료된 게임 세션들 조회
		List<GameSession> gameSessions = gameSessionRepository.findCompletedGamesByPeriod(startTime, endTime);

		// 사용자별로 그룹화하여 게임 수 카운트
		Map<RiotUser, Long> gameCountByUser = gameSessions.stream()
														  .collect(Collectors.groupingBy(
															  GameSession::getRiotUser,
															  Collectors.counting()
														  ));

		// 모든 모니터링 유저에 대해 통계 생성 (0판인 경우도 포함)
		return monitoredUsers.stream()
							 .map(user -> new DailyUserGameStats(
								 user.getDisplayName(),
								 gameCountByUser.getOrDefault(user, 0L).intValue()
							 ))
							 .sorted((a, b) -> Integer.compare(b.totalGames(), a.totalGames())) // 게임 수 내림차순
							 .collect(Collectors.toList());
	}
}
