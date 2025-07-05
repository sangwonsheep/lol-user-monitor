package project.lolmonitor.service.riot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.riot.api.RiotAccountApi;
import project.lolmonitor.client.riot.api.RiotSpectatorApi;
import project.lolmonitor.client.riot.dto.Account;
import project.lolmonitor.client.riot.dto.CurrentGameInfo;
import project.lolmonitor.client.riot.dto.CurrentGameParticipant;
import project.lolmonitor.service.riot.dto.GameSession;
import project.lolmonitor.service.notification.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiotService {

	private final RiotAccountApi riotAccountApi;
	private final RiotSpectatorApi riotSpectatorApi;
	private final NotificationService notificationService;

	// GameSession 객체로 더 상세한 정보 저장
	private final Map<String, GameSession> activeGameSessions = new ConcurrentHashMap<>();

	// PUUID 캐시 (안전하게 정리 가능)
	private final Map<String, String> puuidCache = new ConcurrentHashMap<>();

	public void checkGameStatus(String gameName, String tagLine) {
		String playerDisplayName = gameName + "#" + tagLine;
		log.info("🔍 {}의 게임 상태 확인 중...", playerDisplayName);

		try {
			// 1. PUUID 획득 (캐시 활용)
			String puuid = getOrFetchPuuid(gameName, tagLine);
			log.info("PUUID: {}", puuid);

			// 2. 현재 게임 상태 확인
			checkCurrentGameStatus(playerDisplayName, puuid);

		} catch (Exception e) {
			log.error("❌ {} 상태 확인 중 오류 발생: {}", playerDisplayName, e.getMessage());
		}
	}

	private String getOrFetchPuuid(String gameName, String tagLine) {
		String cacheKey = gameName + "#" + tagLine;

		String cachedPuuid = puuidCache.get(cacheKey);
		if (cachedPuuid != null) {
			log.debug("📋 캐시에서 PUUID 조회: {}", cacheKey);
			return cachedPuuid;
		}

		log.info("🌐 Account API 호출: {}", cacheKey);
		Account account = riotAccountApi.getAccountByRiotId(gameName, tagLine);
		String puuid = account.puuid();

		puuidCache.put(cacheKey, puuid);
		log.info("💾 PUUID 캐시 저장 완료");

		return puuid;
	}

	private void checkCurrentGameStatus(String playerDisplayName, String puuid) {
		try {
			// Spectator API 호출 (200: 게임 중, 404: 게임 중이 아님)
			CurrentGameInfo currentGame = riotSpectatorApi.getCurrentGameBySummoner(puuid);

			log.info("🎮 {} 현재 게임 중! 게임ID: {}", playerDisplayName, currentGame.gameId());
			handleGameInProgress(playerDisplayName, currentGame, puuid);

		} catch (HttpClientErrorException.NotFound e) {
			log.info("💤 {} 현재 게임 중이 아님", playerDisplayName);
			handleGameEnded(playerDisplayName);

		} catch (HttpClientErrorException.TooManyRequests e) {
			log.warn("⚠️ API 호출 제한 도달. 잠시 후 다시 시도합니다.");

		} catch (HttpClientErrorException.Forbidden e) {
			log.error("🚫 API 권한 오류: {}", e.getMessage());

		} catch (Exception e) {
			log.error("💥 Spectator API 호출 실패: {}", e.getMessage());
		}
	}

	private void handleGameInProgress(String playerDisplayName, CurrentGameInfo currentGame, String puuid) {
		String gameId = String.valueOf(currentGame.gameId());
		GameSession currentSession = activeGameSessions.get(playerDisplayName);

		// 이미 같은 게임으로 알림을 보낸 경우 스킵
		if (currentSession != null && gameId.equals(currentSession.getGameId())) {
			log.debug("🔄 {} 이미 게임ID {} 알림 전송됨 ({}분 경과)",
				playerDisplayName, gameId, currentSession.getGameDurationMinutes());
			return;
		}

		// 게임 시작 시간 계산
		LocalDateTime gameStartTime = currentGame.gameStartTime() != null
			? LocalDateTime.ofInstant(Instant.ofEpochMilli(currentGame.gameStartTime()), ZoneId.systemDefault())
			: LocalDateTime.now();

		// 플레이어 정보 찾기
		CurrentGameParticipant player = findPlayerInGame(currentGame, puuid);

		// 새로운 게임 세션 생성
		GameSession gameSession = GameSession.builder()
			.playerDisplayName(playerDisplayName)
			.puuid(puuid)
			.gameId(gameId)
			.startTime(gameStartTime)
			.gameMode(currentGame.gameMode())
			.mapId(currentGame.mapId())
			.championId(player != null ? player.championId() : null)
			.teamId(player != null ? player.teamId() : null)
			.region("KR") // 기본값, 필요시 설정으로 변경
			.build();

		// 활성 게임 목록에 추가
		activeGameSessions.put(playerDisplayName, gameSession);

		// 🔔 알림 전송
		notificationService.sendGameStartNotification(playerDisplayName, currentGame);

		log.info("🎯 새로운 게임 시작 알림 전송: {} - 게임ID: {} ({})",
			playerDisplayName, gameId, gameSession.getGameMode());
	}

	private void handleGameEnded(String playerDisplayName) {
		GameSession endedSession = activeGameSessions.remove(playerDisplayName);

		if (endedSession != null) {
			long durationMinutes = endedSession.getGameDurationMinutes();

			// 게임 종료 알림 전송 (NotificationService에 해당 메서드가 있다면)
			// notificationService.sendGameEndNotification(playerDisplayName, durationMinutes);

			log.info("🏁 {} 게임 종료 감지 - 게임시간: {}분, 모드: {}",
				playerDisplayName, durationMinutes, endedSession.getGameMode());
		}
	}

	private CurrentGameParticipant findPlayerInGame(CurrentGameInfo currentGame, String puuid) {
		return currentGame.participants().stream()
						  .filter(p -> puuid.equals(p.puuid()))
						  .findFirst()
						  .orElse(null);
	}

	/**
	 * 현재 활성 게임 목록 조회 (GameSession 객체 반환)
	 */
	public Map<String, GameSession> getActiveGames() {
		return Map.copyOf(activeGameSessions);
	}

	/**
	 * 비정상적으로 오래된 게임 세션 정리
	 */
	public void cleanupStaleGames() {
		List<String> staleGames = activeGameSessions.entrySet().stream()
													.filter(entry -> entry.getValue().isStale()) // 2시간 이상인 경우
													.map(Map.Entry::getKey)
													.collect(Collectors.toList());

		for (String playerName : staleGames) {
			GameSession staleSession = activeGameSessions.remove(playerName);
			log.warn("🗑️ 비정상적으로 오래된 게임 세션 정리: {} ({}분 경과, 게임ID: {})",
				playerName, staleSession.getGameDurationMinutes(), staleSession.getGameId());
		}

		if (!staleGames.isEmpty()) {
			log.info("🧹 {}개의 오래된 게임 세션 정리 완료", staleGames.size());
		} else {
			log.debug("정리할 오래된 게임 세션 없음");
		}
	}

	/**
	 * PUUID 캐시만 안전하게 정리
	 */
	public void clearPuuidCache() {
		int cacheSize = puuidCache.size();
		puuidCache.clear();
		log.info("🗑️ PUUID 캐시 정리 완료: {}개 항목 삭제", cacheSize);
	}

	/**
	 * 전체 캐시 초기화 (관리자용)
	 */
	public void clearAllCache() {
		int activeCount = activeGameSessions.size();
		int puuidCount = puuidCache.size();

		activeGameSessions.clear();
		puuidCache.clear();

		log.warn("🚨 전체 캐시 초기화: 활성게임 {}개, PUUID {}개 삭제", activeCount, puuidCount);
	}

	/**
	 * 활성 게임 상태 요약 정보
	 */
	public Map<String, Object> getGameStatusSummary() {
		Map<String, Object> summary = new HashMap<>();

		// 전체 통계
		summary.put("totalActiveGames", activeGameSessions.size());
		summary.put("puuidCacheSize", puuidCache.size());

		// 게임 모드별 통계
		Map<String, Long> gameModeStats = activeGameSessions.values().stream()
															.collect(Collectors.groupingBy(
																GameSession::getGameMode,
																Collectors.counting()
															));
		summary.put("gameModeStats", gameModeStats);

		// 플레이 시간별 통계
		Map<String, Long> durationStats = activeGameSessions.values().stream()
															.collect(Collectors.groupingBy(
																session -> {
																	long minutes = session.getGameDurationMinutes();
																	if (minutes < 15) return "0-15분";
																	else if (minutes < 30) return "15-30분";
																	else if (minutes < 60) return "30-60분";
																	else return "60분+";
																},
																Collectors.counting()
															));
		summary.put("durationStats", durationStats);

		// 비정상 세션 수
		long staleCount = activeGameSessions.values().stream()
											.mapToLong(session -> session.isStale() ? 1 : 0)
											.sum();
		summary.put("staleGameCount", staleCount);

		return summary;
	}

	/**
	 * 특정 플레이어의 현재 게임 정보 조회
	 */
	public Optional<GameSession> getPlayerCurrentGame(String playerDisplayName) {
		return Optional.ofNullable(activeGameSessions.get(playerDisplayName));
	}
}
