package project.lolmonitor.service.riot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.riot.api.RiotKoreaApi;
import project.lolmonitor.client.riot.dto.Summoner;
import project.lolmonitor.infra.riot.datahandler.GameSessionDataHandler;
import project.lolmonitor.infra.riot.datahandler.SummonerLevelHistoryDataHandler;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.entity.SummonerLevelHistory;
import project.lolmonitor.service.notification.GameNotificationService;

/**
 * 소환사 레벨 업 확인하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummonerLevelService {

	private final RiotKoreaApi riotKoreaApi;
	private final GameNotificationService gameNotificationService;
	private final SummonerLevelHistoryDataHandler levelHistoryDataHandler;
	private final GameSessionDataHandler gameSessionDataHandler;

	/**
	 * 소환사 레벨 확인 및 레벨업 감지
	 */
	public void checkSummonerLevel(RiotUser riotUser) {
		try {
			log.info("📊 {}의 레벨 확인 중...", riotUser.getDisplayName());

			// 1. 현재 API에서 레벨 조회
			Summoner summoner = riotKoreaApi.getSummoner(riotUser.getPuuid());
			int currentLevel = summoner.summonerLevel().intValue();

			// 2. 마지막 기록된 레벨 조회
			int lastRecordedLevel = levelHistoryDataHandler.getLastRecordedLevel(riotUser);

			// 3. 첫 번째 레벨 기록인지 확인
			boolean isFirstRecord = lastRecordedLevel == 0;

			if (isFirstRecord) {
				// 첫 기록: 현재 레벨을 기준점으로 저장 (레벨업 알림 없음)
				handleFirstLevelRecord(riotUser, currentLevel);
			} else if (currentLevel > lastRecordedLevel) {
				// 실제 레벨업 감지
				handleLevelUp(riotUser, lastRecordedLevel, currentLevel);
			}

		} catch (Exception e) {
			log.error("❌ {} 레벨 확인 실패: {}", riotUser.getDisplayName(), e.getMessage());
		}
	}

	/**
	 * 레벨업 처리
	 */
	private void handleLevelUp(RiotUser riotUser, int previousLevel, int currentLevel) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime lastLevelUpTime = levelHistoryDataHandler.getLastLevelUpTime(riotUser);

		BigDecimal timeTaken = calculateTimeTaken(lastLevelUpTime, now);
		int gamesSinceLastLevelUp = countGamesSinceLastLevelUp(riotUser, lastLevelUpTime);

		// 레벨업 이력 저장
		SummonerLevelHistory levelHistory = levelHistoryDataHandler.saveLevelHistory(
			riotUser, currentLevel, now, gamesSinceLastLevelUp, timeTaken);

		// 디스코드 알림 전송
		gameNotificationService.sendLevelUpNotification(riotUser.getDisplayName(), previousLevel, levelHistory);

		log.info("🎉 {} 레벨업! {} → {} ({}시간, {}판)",
			riotUser.getDisplayName(), previousLevel, currentLevel, timeTaken, gamesSinceLastLevelUp);
	}

	/**
	 * 첫 번째 레벨 기록 (알림 없음)
	 */
	private void handleFirstLevelRecord(RiotUser riotUser, int currentLevel) {
		LocalDateTime now = LocalDateTime.now();

		// 첫 기록이므로 시간과 게임 수는 0으로 설정
		SummonerLevelHistory levelHistory = levelHistoryDataHandler.saveLevelHistory(
			riotUser, currentLevel, now, 0, BigDecimal.ZERO);

		log.info("🆕 {} 첫 레벨 기록: {}레벨", riotUser.getDisplayName(), currentLevel);
	}

	/**
	 * 마지막 레벨업 이후 완료된 게임 수 카운트
	 */
	private int countGamesSinceLastLevelUp(RiotUser riotUser, LocalDateTime since) {
		return gameSessionDataHandler.countCompletedGamesSince(riotUser.getId(), since);
	}

	/**
	 * 레벨업에 소요된 시간 계산 (시간 단위)
	 */
	private BigDecimal calculateTimeTaken(LocalDateTime startTime, LocalDateTime endTime) {
		Duration duration = Duration.between(startTime, endTime);
		long totalSeconds = duration.getSeconds();
		return BigDecimal.valueOf(totalSeconds)
						 .divide(BigDecimal.valueOf(3600), 4, RoundingMode.HALF_UP); // 3600초 = 1시간
	}
}