package project.lolmonitor.client.riot.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import project.lolmonitor.client.riot.dto.Account;

/**
 * https://asia.api.riotgames.com
 */
public interface RiotAsiaApi {

	// 사용자 정보 조회 (puuid 획득 용도)
	@GetExchange("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
	Account getAccountByRiotId(@PathVariable String gameName, @PathVariable String tagLine);
}
