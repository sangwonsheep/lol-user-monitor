package project.lolmonitor.client.notification.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.notification.dto.DiscordMessage;
import project.lolmonitor.common.enums.DiscordChannel;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotificationSender {

	@Value("${discord.game-start.url}")
	private String gameStartUrl;

	@Value("${discord.level-up.url}")
	private String levelUpUrl;

	@Value("${discord.daily-statistics.url}")
	private String dailyStatisticsUrl;

	@Value("${discord.weekly-statistics.url}")
	private String weeklyStatisticsUrl;

	@Value("${discord.monthly-statistics.url}")
	private String monthlyStatisticsUrl;

	@Value("${notification.retry.max-attempts:3}")
	private int maxRetryAttempts;

	public void sendNotification(String message, DiscordChannel channel) {
		String webhookUrl = getWebhookUrl(channel);

		if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
			log.info("Discord 웹훅 URL이 설정되지 않았습니다: {}", channel);
			return;
		}

		for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
			try {
				sendToWebhook(webhookUrl, message);

				log.info("✅ Discord 알림 전송 성공 ({}) {}번째 시도", channel, attempt);
				return;

			} catch (Exception e) {
				log.error("❌ Discord 알림 전송 실패 ({}) {}번째 시도: {}", channel, attempt, e.getMessage());

				if (attempt < maxRetryAttempts) {
					try {
						Thread.sleep(2000 * attempt);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}
	}

	private void sendToWebhook(String webhookUrl, String message) {
		RestClient.create()
				  .post()
				  .uri(webhookUrl)
				  .body(new DiscordMessage(message))
				  .retrieve()
				  .toBodilessEntity();
	}

	private String getWebhookUrl(DiscordChannel channel) {
		return switch (channel) {
			case GAME_START -> gameStartUrl;
			case LEVEL_UP -> levelUpUrl;
			case DAILY_STATISTICS -> dailyStatisticsUrl;
			case WEEKLY_STATISTICS -> weeklyStatisticsUrl;
			case MONTHLY_STATISTICS -> monthlyStatisticsUrl;
		};
	}
}
