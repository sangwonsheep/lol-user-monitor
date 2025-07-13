package project.lolmonitor.client.riot.api;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import project.lolmonitor.client.riot.dto.ChampionData;

/**
 * https://ddragon.leagueoflegends.com
 * Data Dragon API 호출
 * 리그 오브 레전드 챔피언 정보 가져오기
 */
public interface RiotDataDragonApi {

	/**
	 * 최신 버전 목록 조회
	 */
	@GetExchange("/api/versions.json")
	List<String> getVersions();

	/**
	 * 챔피언 데이터 조회 (한국어)
	 */
	@GetExchange("/cdn/{version}/data/ko_KR/champion.json")
	ChampionData getChampionData(@PathVariable String version);
}
