package project.lolmonitor.service.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.notification.api.DiscordNotificationSender;
import project.lolmonitor.common.enums.DiscordChannel;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsNotificationService {

	private final DiscordNotificationSender discordNotificationSender;

	// 요일이 포함된 포맷터 (한국어)
	private static final DateTimeFormatter FORMATTER_WITH_WEEKDAY =
		DateTimeFormatter.ofPattern("yyyy-MM-dd(E) HH:mm", Locale.KOREAN);

	// 일간 게임 통계 알림
	public void sendDailyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime, Map<String, Integer> consecutiveDaysMap) {
		String message = createDailyStatisticsMessage(userStats, startTime, endTime, consecutiveDaysMap);
		discordNotificationSender.sendNotification(message, DiscordChannel.DAILY_STATISTICS);
	}

	// 주간 게임 통계 알림
	public void sendWeeklyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createWeeklyStatisticsMessage(userStats, startTime, endTime);
		discordNotificationSender.sendNotification(message, DiscordChannel.WEEKLY_STATISTICS);
	}

	// 월간 게임 통계 알림
	public void sendMonthlyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createMonthlyStatisticsMessage(userStats, startTime, endTime);
		discordNotificationSender.sendNotification(message, DiscordChannel.MONTHLY_STATISTICS);
	}

	private String createDailyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime, Map<String, Integer> consecutiveDaysMap) {
		return createStatisticsMessage(
			"🗓️ **일일 게임 통계** 🗓️",
			"📅",
			userStats,
			startTime,
			endTime,
			consecutiveDaysMap
		);
	}

	private String createWeeklyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		return createStatisticsMessage(
			"🗓️ **주간 게임 통계** 🗓️",
			"📅",
			userStats,
			startTime,
			endTime,
			null
		);
	}

	private String createMonthlyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		return createStatisticsMessage(
			"🗓️ **월간 게임 통계** 🗓️",
			"📆",
			userStats,
			startTime,
			endTime,
			null
		);
	}

	private String createStatisticsMessage(String title, String periodIcon, List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime, Map<String, Integer> consecutiveDaysMap) {
		String header = String.format("""
            %s
            
            %s **기간**
               •  %s ~ %s
            
            🏆 **플레이어 통계**
            """,
			title,
			periodIcon,
			startTime.format(FORMATTER_WITH_WEEKDAY),
			endTime.format(FORMATTER_WITH_WEEKDAY)
		);

		StringBuilder message = new StringBuilder(header);

		for (DailyUserGameStats stats : userStats) {
			StringBuilder playerLine = new StringBuilder();
			playerLine.append(String.format("   •  **%s**: %d판",
				stats.playerName(),
				stats.totalGames()
			));

			// 연속 게임 일수 추가 (일일 통계이고, 게임한 유저만)
			if (consecutiveDaysMap != null && stats.totalGames() > 0) {
				Integer consecutiveDays = consecutiveDaysMap.get(stats.playerName());
				if (consecutiveDays != null && consecutiveDays >= 1) {
					playerLine.append(String.format(" 🔥 **%d일 연속!**", consecutiveDays));
				}
			}

			playerLine.append("\n");
			message.append(playerLine);
		}

		return message.toString();
	}
}