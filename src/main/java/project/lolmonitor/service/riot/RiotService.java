package project.lolmonitor.service.riot;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.riot.api.RiotAccountApi;
import project.lolmonitor.client.riot.api.RiotSpectatorApi;
import project.lolmonitor.client.riot.dto.Account;
import project.lolmonitor.client.riot.dto.CurrentGameInfo;
import project.lolmonitor.client.riot.dto.CurrentGameParticipant;
import project.lolmonitor.infra.riot.datahandler.GameSessionDataHandler;
import project.lolmonitor.infra.riot.datahandler.RiotUserDataHandler;
import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.service.notification.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiotService {

	private final RiotAccountApi riotAccountApi;
	private final RiotSpectatorApi riotSpectatorApi;
	private final NotificationService notificationService;
	private final RiotUserDataHandler riotUserDataHandler;
	private final GameSessionDataHandler gameSessionDataHandler;

	public void checkGameStatus(String gameNickName, String tagLine) {
		String playerDisplayName = gameNickName + "#" + tagLine;
		log.info("🔍 {}의 게임 상태 확인 중...", playerDisplayName);

		try {
			// 1. PUUID 획득
			String puuid = getPuuid(gameNickName, tagLine);

			// 2. 현재 게임 상태 확인
			checkCurrentGameStatus(playerDisplayName, puuid);
		} catch (Exception e) {
			log.error("❌ {} 상태 확인 중 오류 발생: {}", playerDisplayName, e.getMessage());
		}
	}

	public void enableRiotUserMonitoring(String gameNickName, String tagLine) {
		riotUserDataHandler.enableRiotUserMonitoring(gameNickName, tagLine);
	}

	public void disableRiotUserMonitoring(String gameNickName, String tagLine) {
		riotUserDataHandler.disableRiotUserMonitoring(gameNickName, tagLine);
	}

	private String getPuuid(String gameNickName, String tagLine) {
		String cacheKey = gameNickName + "#" + tagLine;
		boolean existsRiotUser = riotUserDataHandler.existsRiotUser(gameNickName, tagLine);

		if (existsRiotUser) {
			return riotUserDataHandler.getPuuid(gameNickName, tagLine);
		}

		try {
			// 유저 정보 조회 API 호출
			log.info("🌐 Account API 호출: {}", cacheKey);
			Account account = riotAccountApi.getAccountByRiotId(gameNickName, tagLine);
			String puuid = account.puuid();

			riotUserDataHandler.createRiotUser(gameNickName, tagLine, puuid);
			return puuid;
		} catch (Exception e) {
			log.error("❌ Account API 호출 실패: {}#{} - {}", gameNickName, tagLine, e.getMessage());
			throw e;
		}
	}

	// 현재 게임 상태 확인
	private void checkCurrentGameStatus(String playerDisplayName, String puuid) {
		try {
			// 게임 중 상태 확인 API 호출 (200: 게임 중, 404: 게임 중이 아님)
			CurrentGameInfo currentGame = riotSpectatorApi.getCurrentGameBySummoner(puuid);

			log.info("🎮 {} 현재 게임 중! 게임ID: {}", playerDisplayName, currentGame.gameId());
			handleGameInProgress(playerDisplayName, currentGame, puuid);
		} catch (HttpClientErrorException.NotFound e) {
			log.info("💤 {} 현재 게임 중이 아님", playerDisplayName);
		} catch (HttpClientErrorException.TooManyRequests e) {
			log.warn("⚠️ API 호출 제한 도달. 잠시 후 다시 시도합니다.");
		} catch (HttpClientErrorException.Forbidden e) {
			log.error("🚫 API 권한 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.error("💥 Spectator API 호출 실패: {}", e.getMessage());
		}
	}

	// 게임 중인지 확인
	private void handleGameInProgress(String playerDisplayName, CurrentGameInfo currentGame, String puuid) {
		boolean gameProgressStatus = gameSessionDataHandler.existsGameSession(currentGame.gameId());

		// 이미 존재하는 게임
		if (gameProgressStatus) {
			log.debug("🔄 {}, 게임ID {} 이미 존재하는 게임", playerDisplayName, currentGame.gameId());
			return;
		}

		// 새로운 게임 시작
		try {
			CurrentGameParticipant player = findPlayerInGame(currentGame, puuid);

			// DB에 게임 세션 저장
			GameSession gameSession = gameSessionDataHandler.startGameSession(currentGame, player, puuid);

			// 알림 전송
			notificationService.sendGameStartNotification(playerDisplayName, gameSession);

			log.info("🎯 새 게임 시작 - DB저장 & 알림전송: {} (게임ID: {}, 모드: {})",
				playerDisplayName, currentGame.gameId(), gameSession.getGameMode());

		} catch (Exception e) {
			log.error("❌ 게임 세션 처리 실패: {} - {}", playerDisplayName, e.getMessage());
		}
	}

	// 현재 게임 중인 플레이어와 모니터링하는 플레이어 명이 일치한지
	private CurrentGameParticipant findPlayerInGame(CurrentGameInfo currentGame, String puuid) {
		return currentGame.participants().stream()
						  .filter(p -> puuid.equals(p.puuid()))
						  .findFirst()
						  .orElse(null);
	}
}
