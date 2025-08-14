package project.lolmonitor.service.notification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.notification.api.DiscordNotificationSender;
import project.lolmonitor.common.enums.DiscordChannel;
import project.lolmonitor.common.enums.GameMode;
import project.lolmonitor.infra.riot.datahandler.ChampionDataHandler;
import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.infra.riot.entity.SummonerLevelHistory;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameNotificationService {

	private final DiscordNotificationSender discordNotificationSender;
	private final ChampionDataHandler championDataHandler;

	private static final DateTimeFormatter FORMATTER_WITH_WEEKDAY =
		DateTimeFormatter.ofPattern("yyyy-MM-dd(E) HH:mm", Locale.KOREAN);

	// 게임 시작 알림
	public void sendGameStartNotification(String playerName, GameSession gameSession, int todayGameCount, int gameCount) {
		String message = createGameStartMessage(playerName, gameSession, todayGameCount, gameCount);
		discordNotificationSender.sendNotification(message, DiscordChannel.GAME_START);
	}

	// 레벨업 알림
	public void sendLevelUpNotification(String playerName, int previousLevel, SummonerLevelHistory levelHistory) {
		String message = createLevelUpMessage(playerName, previousLevel, levelHistory);
		discordNotificationSender.sendNotification(message, DiscordChannel.LEVEL_UP);
	}

	private String createGameStartMessage(String playerName, GameSession gameSession, int todayGameCount, int gameCount) {
		return String.format("""
				 🚨🚨🚨 **게임 시작** 🚨🚨🚨
				
				 📍 **유저 정보**
				 	•	소환사 명 : **%s**
				 	•	오늘 플레이 게임 수 : %d
				 	•	누적 게임 수 : %d
				
				 📍 **게임 정보**
				 	•	시작 시간 : %s
				 	•	챔피언 : 🔥 **%s** 🔥
				 	•	게임 모드 : %s
				
				 🔗 [OP.GG에서 보기](https://op.gg/summoners/kr/%s)
				""",
			playerName,
			todayGameCount,
			gameCount,
			gameSession.getStartTime().format(FORMATTER_WITH_WEEKDAY),
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
			levelHistory.getLevelUpTime().format(FORMATTER_WITH_WEEKDAY),
			formatDuration(levelHistory.getTimeTakenHours()),
			levelHistory.getGamesPlayedForLevelup(),
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
}
