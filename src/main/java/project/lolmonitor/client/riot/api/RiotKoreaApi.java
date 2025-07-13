package project.lolmonitor.client.riot.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import project.lolmonitor.client.riot.dto.CurrentGameInfo;
import project.lolmonitor.client.riot.dto.Summoner;

/**
 * https://kr.api.riotgames.com
 */
public interface RiotKoreaApi {

	// 게임 중 상태 확인 (실시간 게임 중인지 확인)
	@GetExchange("/lol/spectator/v5/active-games/by-summoner/{encryptedPUUID}")
	CurrentGameInfo getCurrentGameBySummoner(@PathVariable String encryptedPUUID);

	// 소환사 레벨 정보 확인
	@GetExchange("/lol/summoner/v4/summoners/by-puuid/{encryptedPUUID}")
	Summoner getSummoner(@PathVariable String encryptedPUUID);
}