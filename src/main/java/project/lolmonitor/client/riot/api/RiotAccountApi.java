package project.lolmonitor.client.riot.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import project.lolmonitor.client.riot.dto.Account;
import project.lolmonitor.client.riot.dto.CurrentGameInfo;

/**
 * https://developer.riotgames.com/apis#account-v1
 * account에서 사용되는 api
 */
public interface RiotAccountApi {

	// 사용자 정보 조회 (puuid 획득 용도)
	@GetExchange("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
	Account getAccountByRiotId(@PathVariable String gameName, @PathVariable String tagLine);
}
