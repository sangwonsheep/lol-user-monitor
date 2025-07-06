package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChampionBasicInfo(
	@JsonProperty("version") String version,
	@JsonProperty("id") String id,
	@JsonProperty("key") String key,
	@JsonProperty("name") String name,
	@JsonProperty("title") String title
) {
}
