package project.lolmonitor.client.riot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Perks(
	// IDs of the perks/runes assigned.
	@JsonProperty("perkIds") List<Long> perkIds,

	// Primary runes path
	@JsonProperty("perkStyle") Long perkStyle,

	// Secondary runes path
	@JsonProperty("perkSubStyle") Long perkSubStyle
) {}