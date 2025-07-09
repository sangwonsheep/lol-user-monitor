package project.lolmonitor.service.notification;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.ChampionDataHandler;
import project.lolmonitor.infra.riot.entity.GameSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final RestClient restClient;
	private final ChampionDataHandler championDataHandler;

	@Value("${discord.url}")
	private String discordUrl;

	@Value("${notification.retry.max-attempts:3}")
	private int maxRetryAttempts;

	private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	public void sendGameStartNotification(String playerName, GameSession gameSession, int gameCount) {
		String message = createGameStartMessage(playerName, gameSession, gameCount);
		sendDiscordNotification(message);
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
			getGameModeKorean(gameSession.getGameMode()),
			playerName.replace("#", "-")
		);
	}

	private String getChampionName(String championKey) {
		String championName = championDataHandler.getChampionName(championKey);
		if (championName == null || championName.isEmpty()) {
			return "";
		}

		return championName;
	}

	private String getGameModeKorean(String gameMode) {
		if (gameMode == null)
			return "알 수 없는 모드";

		return switch (gameMode) {
			case "CLASSIC" -> "소환사의 협곡";
			case "ARAM" -> "무작위 총력전";
			case "URF" -> "우르프";
			case "ONEFORALL" -> "원 포 올";
			case "NEXUSBLITZ" -> "넥서스 블리츠";
			case "CHERRY" -> "아레나";
			default -> gameMode;
		};
	}

	private void sendDiscordNotification(String message) {
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
