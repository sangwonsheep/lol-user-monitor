package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 벤 처리된 챔피언
 */
public record BannedChampion(
	// The turn during which the champion was banned
	@JsonProperty("pickTurn") Integer pickTurn,

	// The ID of the banned champion
	@JsonProperty("championId") Long championId,

	// The ID of the team that banned the champion
	@JsonProperty("teamId") Long teamId
) {}