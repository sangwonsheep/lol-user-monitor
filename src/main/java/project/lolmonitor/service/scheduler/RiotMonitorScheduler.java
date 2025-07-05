package project.lolmonitor.service.scheduler;

import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.service.riot.dto.GameSession;
import project.lolmonitor.service.riot.RiotService;

@Component
@Slf4j
@RequiredArgsConstructor
public class RiotMonitorScheduler {

	private final RiotService riotService;

	private final String riotUserName = "건전한소환사mq2#kr1";

	/**
	 * 주기적으로 모든 플레이어 게임 상태 확인
	 */
	@Scheduled(fixedDelayString = "60000")
	public void checkAllPlayers() {
		try {
			String[] parts = riotUserName.split("#");
			String gameName = parts[0].trim();
			String tagLine = parts[1].trim();

			riotService.checkGameStatus(gameName, tagLine);
		} catch (Exception e) {
			log.error("❌ {} 모니터링 실패: {}", riotUserName, e.getMessage());
		}

		log.info("✅ 정기 모니터링 완료");
	}

	/**
	 * 30분마다 오래된 게임 세션 정리
	 */
	@Scheduled(fixedRate = 1800000) // 30분
	public void cleanupStaleGames() {
		log.info("🧹 오래된 게임 세션 정리 시작");
		riotService.cleanupStaleGames();
	}

	/**
	 * 6시간마다 PUUID 캐시만 정리 (활성 게임 정보는 보존)
	 */
	@Scheduled(fixedRate = 21600000) // 6시간
	public void cleanupPuuidCache() {
		log.info("🗑️ PUUID 캐시 정리 실행");
		riotService.clearPuuidCache();
	}

	/**
	 * 자정마다 상태 리포트
	 */
	@Scheduled(cron = "0 0 0 * * *") // 매일 자정
	public void dailyStatusReport() {
		log.info("🌙 일일 상태 리포트");

		Map<String, GameSession> activeGames = riotService.getActiveGames();
		Map<String, Object> summary = riotService.getGameStatusSummary();

		log.info("📊 활성 게임 수: {}", activeGames.size());
		log.info("📊 게임 모드별 통계: {}", summary.get("gameModeStats"));
		log.info("📊 플레이 시간별 통계: {}", summary.get("durationStats"));
		log.info("📊 비정상 세션 수: {}", summary.get("staleGameCount"));

		// 개별 게임 상세 정보 (5개까지만)
		activeGames.entrySet().stream()
				   .limit(5)
				   .forEach(entry -> {
					   GameSession session = entry.getValue();
					   log.info("🎮 {}: {}분 경과, {}, 게임ID: {}",
						   entry.getKey(),
						   session.getGameDurationMinutes(),
						   session.getGameMode(),
						   session.getGameId());
				   });

		if (activeGames.size() > 5) {
			log.info("... 및 {}개 더", activeGames.size() - 5);
		}
	}
}
