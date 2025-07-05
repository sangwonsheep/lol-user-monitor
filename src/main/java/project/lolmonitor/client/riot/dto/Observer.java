package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Observer(
	// Key used to decrypt the spectator grid game data for playback
	@JsonProperty("encryptionKey") String encryptionKey
) {}