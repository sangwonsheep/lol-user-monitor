package project.lolmonitor.service.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

	private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	// 일간 게임 통계 알림
	public void sendDailyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createDailyStatisticsMessage(userStats, startTime, endTime);
		discordNotificationSender.sendNotification(message, DiscordChannel.STATISTICS);
	}

	// 주간 게임 통계 알림
	public void sendWeeklyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createWeeklyStatisticsMessage(userStats, startTime, endTime);
		discordNotificationSender.sendNotification(message, DiscordChannel.STATISTICS);
	}

	// 월간 게임 통계 알림
	public void sendMonthlyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createMonthlyStatisticsMessage(userStats, startTime, endTime);
		discordNotificationSender.sendNotification(message, DiscordChannel.STATISTICS);
	}

	private String createDailyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		return createStatisticsMessage(
			"📊 **일일 게임 통계** 📊",
			"🌅",
			userStats,
			startTime,
			endTime
		);
	}

	private String createWeeklyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		return createStatisticsMessage(
			"📈 **주간 게임 통계** 📈",
			"📅",
			userStats,
			startTime,
			endTime
		);
	}

	private String createMonthlyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		return createStatisticsMessage(
			"🗓️ **월간 게임 통계** 🗓️",
			"📆",
			userStats,
			startTime,
			endTime
		);
	}

	private String createStatisticsMessage(String title, String periodIcon, List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String header = String.format("""
            %s
            
            %s **기간**
               •  %s ~ %s
            
            🏆 **플레이어 통계**
            """,
			title,
			periodIcon,
			startTime.format(SIMPLE_FORMATTER),
			endTime.format(SIMPLE_FORMATTER)
		);

		StringBuilder message = new StringBuilder(header);

		for (DailyUserGameStats stats : userStats) {
			message.append(String.format("   •  **%s**: %d판\n",
				stats.playerName(),
				stats.totalGames()
			));
		}

		return message.toString();
	}
}