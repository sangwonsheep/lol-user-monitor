package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameCustomizationObject(
	// Category identifier for Game Customization
	@JsonProperty("category") String category,

	// Game Customization content
	@JsonProperty("content") String content
) {}