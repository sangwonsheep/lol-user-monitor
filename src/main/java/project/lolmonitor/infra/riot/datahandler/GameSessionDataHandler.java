package project.lolmonitor.infra.riot.datahandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.lolmonitor.client.riot.dto.CurrentGameInfo;
import project.lolmonitor.client.riot.dto.CurrentGameParticipant;
import project.lolmonitor.infra.riot.entity.GameSession;
import project.lolmonitor.infra.riot.entity.GameStatus;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.repository.GameSessionRepository;
import project.lolmonitor.infra.riot.repository.RiotUserRepository;

@Component
@RequiredArgsConstructor
public class GameSessionDataHandler {

	private final GameSessionRepository gameSessionRepository;
	private final RiotUserRepository riotUserRepository;

	@Transactional(readOnly = true)
	public boolean existsGameSession(Long gameId) {
		return gameSessionRepository.existsByGameId(gameId);
	}

	@Transactional
	public GameSession startGameSession(CurrentGameInfo gameInfo, CurrentGameParticipant participant, String puuid) {
		RiotUser riotUser = riotUserRepository.findByPuuid(puuid)
											  .orElseThrow(() -> new RuntimeException(
												  "존재하지 않는 사용자이거나 유효하지 않은 puuid : " + puuid));

		LocalDateTime gameStartTime = gameInfo.gameStartTime() != null
			? LocalDateTime.ofInstant(Instant.ofEpochMilli(gameInfo.gameStartTime()), ZoneId.systemDefault())
			: LocalDateTime.now();

		GameSession gameSession = GameSession.createGameSession(riotUser, gameInfo.gameId(), gameInfo.gameType(),
			gameInfo.gameMode(), gameInfo.gameLength(), GameStatus.IN_PROGRESS, gameInfo.mapId(), participant.championId(), participant.teamId(),
			gameStartTime);
		return gameSessionRepository.save(gameSession);
	}
}
