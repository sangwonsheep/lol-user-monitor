package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Summoner(
	// Encrypted PUUID. Exact length of 78 characters.
	@JsonProperty("puuid") String puuid,

	// ID of the summoner icon associated with the summoner.
	@JsonProperty("profileIconId") int profileIconId,

	// Date summoner was last modified specified as epoch milliseconds.
	// The following events will update this timestamp: profile icon change, playing the tutorial or advanced tutorial, finishing a game, summoner name change.
	@JsonProperty("revisionDate") Long revisionDate,

	// Summoner level associated with the summoner.
	@JsonProperty("summonerLevel") Long summonerLevel
) {
}
