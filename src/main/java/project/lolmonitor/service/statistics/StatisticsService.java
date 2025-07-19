package project.lolmonitor.service.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.GameSessionDataHandler;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;
import project.lolmonitor.service.notification.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

	private final GameSessionDataHandler gameSessionDataHandler;
	private final NotificationService notificationService;

	/**
	 * 전날 18:00 ~ 오늘 18:00 게임 통계 생성 및 전송
	 */
	public void sendDailyGameStatistics() {
		try {
			LocalDateTime endTime = LocalDate.now().atTime(18, 0); // 오늘 18:00
			LocalDateTime startTime = endTime.minusDays(1); // 전날 18:00

			log.info("📊 일일 게임 통계 생성: {} ~ {}", startTime, endTime);

			List<DailyUserGameStats> userStats = gameSessionDataHandler.getDailyGameStatistics(startTime, endTime);

			if (userStats.isEmpty()) {
				log.info("📊 통계 기간 내 게임 데이터가 없습니다.");
				return;
			}

			notificationService.sendDailyStatisticsNotification(userStats, startTime, endTime);

			log.info("✅ 일일 게임 통계 알림 전송 완료 - {}명", userStats.size());

		} catch (Exception e) {
			log.error("❌ 일일 게임 통계 처리 실패: {}", e.getMessage(), e);
		}
	}
}
