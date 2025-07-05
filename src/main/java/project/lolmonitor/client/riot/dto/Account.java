package project.lolmonitor.client.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 아이디 + 태그로 요청 시, 계정에 대한 정보를 받는 객체
 * 예시) 게임관전중#kr1
 */
public record Account(
	// Encrypted PUUID. Exact length of 78 characters.
	@JsonProperty("puuid") String puuid,

	// This field may be excluded from the response if the account doesn't have a gameName.
	@JsonProperty("gameName") String gameName,

	// This field may be excluded from the response if the account doesn't have a tagLine.
	@JsonProperty("tagLine") String tagLine
) {}

