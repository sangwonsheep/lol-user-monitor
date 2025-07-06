package project.lolmonitor.client.riot.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChampionData(
	@JsonProperty("type") String type,
	@JsonProperty("format") String format,
	@JsonProperty("version") String version,
	@JsonProperty("data") Map<String, ChampionBasicInfo> data
) {
}
