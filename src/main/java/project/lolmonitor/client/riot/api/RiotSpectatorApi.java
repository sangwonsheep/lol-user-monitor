package project.lolmonitor.client.riot.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import project.lolmonitor.client.riot.dto.CurrentGameInfo;

/**
 * https://developer.riotgames.com/apis#spectator-v5
 * spectator에서 사용되는 api
 */
public interface RiotSpectatorApi {

	// 게임 중 상태 확인 (실시간 게임 중인지 확인)
	@GetExchange("/lol/spectator/v5/active-games/by-summoner/{encryptedPUUID}")
	CurrentGameInfo getCurrentGameBySummoner(@PathVariable String encryptedPUUID);
}