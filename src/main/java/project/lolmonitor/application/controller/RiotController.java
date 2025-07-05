package project.lolmonitor.application.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.lolmonitor.service.riot.dto.GameSession;
import project.lolmonitor.service.riot.RiotService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/riot")
public class RiotController {

	private final RiotService riotService;

	/**
	 * 수동으로 특정 플레이어 상태 확인
	 */
	@GetMapping
	public ResponseEntity<String> checkGameStatus(@RequestParam String gameName, @RequestParam String tagLine) {
		riotService.checkGameStatus(gameName, tagLine);
		return ResponseEntity.ok("✅ " + gameName + "#" + tagLine + " 상태 확인 완료");
	}

	/**
	 * 현재 게임 중인 플레이어 목록 조회 (상세 정보)
	 */
	@GetMapping("/active-games")
	public ResponseEntity<Map<String, GameSession>> getActiveGames() {
		Map<String, GameSession> activeGames = riotService.getActiveGames();
		return ResponseEntity.ok(activeGames);
	}

	/**
	 * 게임 상태 요약 정보
	 */
	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> getGameStatusSummary() {
		Map<String, Object> summary = riotService.getGameStatusSummary();
		summary.put("timestamp", LocalDateTime.now());
		summary.put("status", "running");
		return ResponseEntity.ok(summary);
	}

	/**
	 * 특정 플레이어의 현재 게임 정보
	 */
	@GetMapping("/player/{gameName}/{tagLine}")
	public ResponseEntity<?> getPlayerCurrentGame(
		@PathVariable String gameName,
		@PathVariable String tagLine) {

		String playerDisplayName = gameName + "#" + tagLine;
		Optional<GameSession> currentGame = riotService.getPlayerCurrentGame(playerDisplayName);

		if (currentGame.isPresent()) {
			GameSession session = currentGame.get();
			Map<String, Object> response = new HashMap<>();
			response.put("player", playerDisplayName);
			response.put("isPlaying", true);
			response.put("gameSession", session);
			response.put("duration", session.getGameDurationMinutes() + "분");
			response.put("isStale", session.isStale());

			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("player", playerDisplayName);
			response.put("isPlaying", false);
			response.put("message", "현재 게임 중이 아님");

			return ResponseEntity.ok(response);
		}
	}

	/**
	 * 활성 게임의 간단한 목록 (플레이어명과 게임시간만)
	 */
	@GetMapping("/active-games/simple")
	public ResponseEntity<Map<String, String>> getActiveGamesSimple() {
		Map<String, GameSession> activeGames = riotService.getActiveGames();

		Map<String, String> simpleView = activeGames.entrySet().stream()
													.collect(Collectors.toMap(
														Map.Entry::getKey,
														entry -> {
															GameSession session = entry.getValue();
															return String.format("%s (%d분)",
																session.getGameMode(),
																session.getGameDurationMinutes());
														}
													));

		return ResponseEntity.ok(simpleView);
	}

	/**
	 * PUUID 캐시 정리
	 */
	@PostMapping("/clear-puuid-cache")
	public ResponseEntity<String> clearPuuidCache() {
		riotService.clearPuuidCache();
		return ResponseEntity.ok("🗑️ PUUID 캐시가 정리되었습니다.");
	}

	/**
	 * 오래된 게임 세션 정리
	 */
	@PostMapping("/cleanup-stale-games")
	public ResponseEntity<String> cleanupStaleGames() {
		riotService.cleanupStaleGames();
		return ResponseEntity.ok("🧹 오래된 게임 세션이 정리되었습니다.");
	}

	/**
	 * 전체 캐시 초기화 (관리자용)
	 */
	@PostMapping("/clear-all-cache")
	public ResponseEntity<String> clearAllCache() {
		riotService.clearAllCache();
		return ResponseEntity.ok("🚨 전체 캐시가 초기화되었습니다.");
	}

	/**
	 * 현재 모니터링 중인 플레이어들의 상태
	 */
	@GetMapping("/monitoring-status")
	public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
		Map<String, GameSession> activeGames = riotService.getActiveGames();
		Map<String, Object> summary = riotService.getGameStatusSummary();

		Map<String, Object> status = new HashMap<>();
		status.put("monitoringActive", true);
		status.put("totalPlayers", summary.get("totalActiveGames"));
		status.put("puuidCacheSize", summary.get("puuidCacheSize"));
		status.put("staleGameCount", summary.get("staleGameCount"));
		status.put("lastUpdate", LocalDateTime.now());

		// 각 플레이어별 상태
		Map<String, Map<String, Object>> playerStatuses = new HashMap<>();
		for (Map.Entry<String, GameSession> entry : activeGames.entrySet()) {
			GameSession session = entry.getValue();
			Map<String, Object> playerStatus = new HashMap<>();
			playerStatus.put("gameMode", session.getGameMode());
			playerStatus.put("duration", session.getGameDurationMinutes());
			playerStatus.put("isStale", session.isStale());
			playerStatus.put("gameId", session.getGameId());

			playerStatuses.put(entry.getKey(), playerStatus);
		}
		status.put("players", playerStatuses);

		return ResponseEntity.ok(status);
	}
}
