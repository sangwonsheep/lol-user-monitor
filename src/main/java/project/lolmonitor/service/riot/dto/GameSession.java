package project.lolmonitor.service.riot.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameSession {
	private final String playerDisplayName;
	private final String puuid;
	private final String gameId;
	private final LocalDateTime startTime;
	private final String gameMode;
	private final Long mapId;
	private final Long championId;
	private final Long teamId;
	private final String region; // 지역 정보 추가

	public long getGameDurationMinutes() {
		return java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
	}

	// 게임 진행 상태 확인 (너무 오래된 세션 정리용)
	public boolean isStale() {
		return getGameDurationMinutes() > 120; // 2시간 이상은 비정상
	}
}