package project.lolmonitor.client.riot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 현재 게임 중인 정보를 확인할 수 있는 정보
 * 현재 게임 중이 아니면 404 에러
 */
public record CurrentGameInfo(
	// The ID of the game
	@JsonProperty("gameId") Long gameId,

	// The game type
	@JsonProperty("gameType") String gameType,

	// The game start time represented in epoch milliseconds
	@JsonProperty("gameStartTime") Long gameStartTime,

	// The ID of the map
	@JsonProperty("mapId") Long mapId,

	// The amount of time in seconds that has passed since the game started
	@JsonProperty("gameLength") Long gameLength,

	// The ID of the platform on which the game is being played
	@JsonProperty("platformId") String platformId,

	// The game mode
	@JsonProperty("gameMode") String gameMode,

	// Banned champion information
	@JsonProperty("bannedChampions") List<BannedChampion> bannedChampions,

	// The queue type (queue types are documented on the Game Constants page)
	@JsonProperty("gameQueueConfigId") Long gameQueueConfigId,

	// The observer information
	@JsonProperty("observers") Observer observer,

	// The participant information
	@JsonProperty("participants") List<CurrentGameParticipant> participants
) {}