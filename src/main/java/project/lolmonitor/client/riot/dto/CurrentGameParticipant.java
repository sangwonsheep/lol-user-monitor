package project.lolmonitor.client.riot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrentGameParticipant(
	// The ID of the champion played by this participant
	@JsonProperty("championId") Long championId,

	// Perks/Runes Reforged Information
	@JsonProperty("perks") Perks perks,

	// The ID of the profile icon used by this participant
	@JsonProperty("profileIconId") Long profileIconId,

	// Flag indicating whether or not this participant is a bot
	@JsonProperty("bot") boolean bot,

	// The team ID of this participant, indicating the participant's team
	@JsonProperty("teamId") Long teamId,

	// The encrypted puuid of this participant
	@JsonProperty("puuid") String puuid,

	// The ID of the first summoner spell used by this participant
	@JsonProperty("spell1Id") Long spell1Id,

	// The ID of the second summoner spell used by this participant
	@JsonProperty("spell2Id") Long spell2Id,

	// List of Game Customizations
	@JsonProperty("gameCustomizationObjects") List<GameCustomizationObject> gameCustomizationObjects
) {}