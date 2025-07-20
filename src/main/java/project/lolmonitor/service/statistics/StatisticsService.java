package project.lolmonitor.service.statistics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.GameSessionDataHandler;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;
import project.lolmonitor.service.notification.StatisticsNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

	private final GameSessionDataHandler gameSessionDataHandler;
	private final StatisticsNotificationService notificationService;

	/**
	 *	일간 게임 통계
	 *	전날 18:00 ~ 오늘 18:00 게임 통계 생성 및 전송
	 */
	public void sendDailyGameStatistics() {
		try {
			LocalDateTime endTime = LocalDate.now().atTime(18, 0); // 오늘 18:00
			LocalDateTime startTime = endTime.minusDays(1); // 전날 18:00

			log.info("📊 일일 게임 통계 생성: {} ~ {}", startTime, endTime);

			List<DailyUserGameStats> userStats = gameSessionDataHandler.getGameStatistics(startTime, endTime);

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

	/**
	 *	주간 게임 통계
	 *	지난 주 월요일 09:00 ~ 이번 주 월요일 09:00 (7일간)
	 */
	public void sendWeeklyGameStatistics() {
		try {
			LocalDate today = LocalDate.now();
			LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
			LocalDate lastMonday = thisMonday.minusWeeks(1);

			LocalDateTime startTime = lastMonday.atTime(9, 0); // 지난 주 월요일 09:00
			LocalDateTime endTime = thisMonday.atTime(9, 0);   // 이번 주 월요일 09:00

			log.info("📈 주간 게임 통계 생성: {} ~ {}", startTime, endTime);

			List<DailyUserGameStats> userStats = gameSessionDataHandler.getGameStatistics(startTime, endTime);

			if (userStats.isEmpty()) {
				log.info("📈 지난 주 게임 데이터가 없습니다.");
				return;
			}

			notificationService.sendWeeklyStatisticsNotification(userStats, startTime, endTime);

			log.info("✅ 주간 게임 통계 알림 전송 완료 - {}명", userStats.size());

		} catch (Exception e) {
			log.error("❌ 주간 게임 통계 처리 실패: {}", e.getMessage(), e);
		}
	}

	/**
	 *	월간 게임 통계
	 *	지난 달 1일 09:00 ~ 이번 달 1일 09:00 (한 달간)
	 */
	public void sendMonthlyGameStatistics() {
		try {
			LocalDate today = LocalDate.now();
			LocalDate thisMonth1st = today.withDayOfMonth(1);
			LocalDate lastMonth1st = thisMonth1st.minusMonths(1);

			LocalDateTime startTime = lastMonth1st.atTime(9, 0); // 지난 달 1일 09:00
			LocalDateTime endTime = thisMonth1st.atTime(9, 0);   // 이번 달 1일 09:00

			log.info("📊 월간 게임 통계 생성: {} ~ {}", startTime, endTime);

			List<DailyUserGameStats> userStats = gameSessionDataHandler.getGameStatistics(startTime, endTime);

			if (userStats.isEmpty()) {
				log.info("📊 지난 달 게임 데이터가 없습니다.");
				return;
			}

			notificationService.sendMonthlyStatisticsNotification(userStats, startTime, endTime);

			log.info("✅ 월간 게임 통계 알림 전송 완료 - {}명", userStats.size());

		} catch (Exception e) {
			log.error("❌ 월간 게임 통계 처리 실패: {}", e.getMessage(), e);
		}
	}
}
