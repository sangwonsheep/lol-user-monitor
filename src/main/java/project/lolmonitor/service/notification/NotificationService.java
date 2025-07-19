package project.lolmonitor.service.notification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.common.enums.GameMode;
import project.lolmonitor.infra.riot.datahandler.ChampionDataHandler;
import project.lolmonitor.infra.riot.dto.DailyUserGameStats;
import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.infra.riot.entity.SummonerLevelHistory;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final RestClient restClient;
	private final ChampionDataHandler championDataHandler;

	@Value("${discord.game-start.url}")
	private String gameStartUrl;

	@Value("${discord.level-up.url}")
	private String levelUpUrl;

	@Value("${discord.statistics.url}")
	private String statisticsUrl;

	@Value("${notification.retry.max-attempts:3}")
	private int maxRetryAttempts;

	private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	// 게임 시작 알림
	public void sendGameStartNotification(String playerName, GameSession gameSession, int gameCount) {
		String message = createGameStartMessage(playerName, gameSession, gameCount);
		sendDiscordNotification(message, gameStartUrl);
	}

	// 레벨업 알림
	public void sendLevelUpNotification(String playerName, int previousLevel, SummonerLevelHistory levelHistory) {
		String message = createLevelUpMessage(playerName, previousLevel, levelHistory);
		sendDiscordNotification(message, levelUpUrl);
	}

	// 통계 알림
	public void sendDailyStatisticsNotification(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String message = createDailyStatisticsMessage(userStats, startTime, endTime);
		sendDiscordNotification(message, statisticsUrl);
	}

	private String createGameStartMessage(String playerName, GameSession gameSession, int gameCount) {
		return String.format("""
				 🚨🚨🚨 **게임 시작** 🚨🚨🚨
				
				 📍 **유저 정보**
				 	•	소환사 명 : **%s**
				 	•	누적 판 수 : %d
				
				 📍 **게임 정보**
				 	•	시작 시간 : %s
				 	•	챔피언 : 🔥 **%s** 🔥
				 	•	게임 모드 : %s
				
				 🔗 [OP.GG에서 보기](https://op.gg/summoners/kr/%s)
				""",
			playerName,
			gameCount,
			gameSession.getStartTime().format(SIMPLE_FORMATTER),
			getChampionName(String.valueOf(gameSession.getChampionId())),
			GameMode.getKoreanName(gameSession.getGameMode()),
			playerName.replace("#", "-")
		);
	}

	private String createLevelUpMessage(String playerName, int previousLevel, SummonerLevelHistory levelHistory) {
		return String.format("""
				🎉🎉🎉 **레벨업 축하!** 🎉🎉🎉
				
				📍 **소환사 정보**
					•	소환사 명 : **%s**
					•	레벨 : %d → **%d**
				
				📍 **레벨업 통계**
					•	레벨업 시간 : %s
					•	소요 시간 : **%s**
					•	플레이 판 수 : **%d**
				
				🔗 [OP.GG에서 보기](https://op.gg/summoners/kr/%s)
				""",
			playerName,
			previousLevel,
			levelHistory.getLevel(),
			levelHistory.getLevelUpTime().format(SIMPLE_FORMATTER),
			formatDuration(levelHistory.getTimeTakenHours()),
			levelHistory.getGamesPlayedForLevelup(),
			playerName.replace("#", "-")
		);
	}

	private String createDailyStatisticsMessage(List<DailyUserGameStats> userStats,
		LocalDateTime startTime, LocalDateTime endTime) {
		String header = String.format("""
				📊📊📊 **일일 게임 통계** 📊📊📊
				
				📅 **기간**
				•	%s ~ %s
				
				📋 **플레이어 통계**
				""",
			startTime.format(SIMPLE_FORMATTER),
			endTime.format(SIMPLE_FORMATTER)
		);

		StringBuilder message = new StringBuilder(header);

		for (int i = 0; i < userStats.size(); i++) {
			DailyUserGameStats stats = userStats.get(i);
			message.append(String.format("%d. **%s**: %d판\n",
				i + 1,
				stats.playerName(),
				stats.totalGames()
			));
		}

		return message.toString();
	}

	private String getChampionName(String championKey) {
		String championName = championDataHandler.getChampionName(championKey);
		if (championName == null || championName.isEmpty()) {
			return "";
		}

		return championName;
	}

	private String formatDuration(BigDecimal hours) {
		if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
			return "0분";
		}

		// 분 단위로 변환 (반올림 적용)
		BigDecimal totalMinutesBD = hours.multiply(BigDecimal.valueOf(60));
		int totalMinutes = totalMinutesBD.setScale(0, RoundingMode.HALF_UP).intValue();

		// 시간과 분으로 분리
		int hoursPart = totalMinutes / 60;
		int minutesPart = totalMinutes % 60;

		// 포맷팅
		if (hoursPart > 0 && minutesPart > 0) {
			return String.format("%d시간 %d분", hoursPart, minutesPart);
		} else if (hoursPart > 0) {
			return String.format("%d시간", hoursPart);
		} else {
			return String.format("%d분", minutesPart);
		}
	}

	private void sendDiscordNotification(String message, String discordUrl) {
		if (discordUrl == null || discordUrl.trim().isEmpty()) {
			log.info("Discord 웹훅 URL이 설정되지 않았습니다.");
			return;
		}

		for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
			try {
				Map<String, String> payload = Map.of("content", message);

				restClient.post()
						  .uri(discordUrl)
						  .body(payload)
						  .retrieve()
						  .toBodilessEntity();

				log.info("✅ Discord 알림 전송 성공 ({}번째 시도)", attempt);
				return;

			} catch (Exception e) {
				log.error("❌ Discord 알림 전송 실패 ({}번째 시도): {}", attempt, e.getMessage());

				if (attempt < maxRetryAttempts) {
					try {
						Thread.sleep(2000 * attempt); // 백오프 지연
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}
	}
}
