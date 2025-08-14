package project.lolmonitor.service.statistics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.GameSessionDataHandler;
import project.lolmonitor.infra.riot.datahandler.RiotUserDataHandler;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.service.notification.StatisticsNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

	private final GameSessionDataHandler gameSessionDataHandler;
	private final RiotUserDataHandler riotUserDataHandler;
	private final StatisticsNotificationService notificationService;

	/**
	 *	일간 게임 통계
	 *	전날 08:30 ~ 오늘 08:30 게임 통계 생성 및 전송
	 */
	public void sendDailyGameStatistics() {
		try {
			LocalDateTime endTime = LocalDate.now().atTime(8, 30); // 오늘 08:30
			LocalDateTime startTime = endTime.minusDays(1); // 전날 08:30

			log.info("📊 일일 게임 통계 생성: {} ~ {}", startTime, endTime);

			List<DailyUserGameStats> userStats = gameSessionDataHandler.getGameStatistics(startTime, endTime);

			// 게임한 유저가 있는지 확인 (0판 유저도 포함하되, 모든 유저가 0판이면 전송하지 않음)
			boolean hasAnyGames = userStats.stream().anyMatch(stats -> stats.totalGames() > 0);

			if (!hasAnyGames) {
				log.info("📊 통계 기간 내 게임 데이터가 없습니다.");
				return;
			}

			// 연속 게임 일수 정보 추가
			Map<String, Integer> consecutiveDaysMap = calculateConsecutiveDaysForUsers(userStats, endTime);

			notificationService.sendDailyStatisticsNotification(userStats, startTime, endTime, consecutiveDaysMap);

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

	/**
	 * 각 유저의 연속 게임 일수 계산
	 */
	private Map<String, Integer> calculateConsecutiveDaysForUsers(List<DailyUserGameStats> userStats, LocalDateTime baseTime) {
		return userStats.stream()
						.filter(stats -> stats.totalGames() > 0) // 오늘 게임한 유저만 대상
						.collect(Collectors.toMap(
							DailyUserGameStats::playerName,
							stats -> {
								try {
									// displayName을 "#"으로 분리해서 기존 메서드 사용
									String[] parts = stats.playerName().split("#");
									if (parts.length != 2) {
										log.warn("⚠️ 잘못된 displayName 형식: {}", stats.playerName());
										return 0;
									}

									String gameNickname = parts[0];
									String tagLine = parts[1];

									RiotUser riotUser = riotUserDataHandler.getRiotUser(gameNickname, tagLine);
									return gameSessionDataHandler.calculateConsecutiveGameDays(riotUser.getId(), baseTime);
								} catch (Exception e) {
									log.warn("⚠️ {}의 연속 게임 일수 계산 실패: {}", stats.playerName(), e.getMessage());
									return 0;
								}
							}
						));
	}
}
