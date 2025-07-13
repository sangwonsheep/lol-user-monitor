package project.lolmonitor.infra.riot.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.lolmonitor.common.enums.GameStatus;
import project.lolmonitor.infra.common.BaseEntity;

@Table(name =  "game_session")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "riot_user_id", nullable = false)
	private RiotUser riotUser;

	@Column(name = "game_id", unique = true, nullable = false)
	private Long gameId;

	@Column(name = "game_type")
	private String gameType;

	@Column(name = "game_mode")
	private String gameMode;

	@Column(name = "game_length")
	private Long gameLength;

	@Enumerated(EnumType.STRING)
	@Column(name = "game_status")
	private GameStatus gameStatus;

	@Column(name = "map_id")
	private Long mapId;

	@Column(name = "champion_id")
	private Long championId;

	@Column(name = "team_id")
	private Long teamId;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Builder
	private GameSession(RiotUser riotUser, Long gameId, String gameType, String gameMode, Long gameLength, GameStatus gameStatus, Long mapId, Long championId, Long teamId, LocalDateTime startTime) {
		this.riotUser = riotUser;
		this.gameId = gameId;
		this.gameType = gameType;
		this.gameMode = gameMode;
		this.gameLength = gameLength;
		this.gameStatus = gameStatus;
		this.mapId = mapId;
		this.championId = championId;
		this.teamId = teamId;
		this.startTime = startTime;
	}

	public static GameSession createGameSession(RiotUser riotUser, Long gameId, String gameType, String gameMode, Long gameLength, GameStatus gameStatus, Long mapId, Long championId, Long teamId, LocalDateTime startTime) {
		return GameSession.builder()
			.riotUser(riotUser)
			.gameId(gameId)
			.gameType(gameType)
			.gameMode(gameMode)
			.gameLength(gameLength)
			.gameStatus(gameStatus)
			.mapId(mapId)
			.championId(championId)
			.teamId(teamId)
			.startTime(startTime)
			.build();
	}

	public void endGame(LocalDateTime endTime) {
		this.endTime = endTime;
		this.gameStatus = GameStatus.COMPLETED;
	}

	public Duration getGameDuration() {
		if (endTime == null) {
			return Duration.between(startTime, LocalDateTime.now());
		}
		return Duration.between(startTime, endTime);
	}
}
