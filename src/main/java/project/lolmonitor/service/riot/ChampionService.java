package project.lolmonitor.service.riot;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.riot.api.RiotDataDragonApi;
import project.lolmonitor.client.riot.dto.ChampionBasicInfo;
import project.lolmonitor.client.riot.dto.ChampionData;
import project.lolmonitor.infra.riot.datahandler.ChampionDataHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionService {

	private final RiotDataDragonApi riotDataDragonApi;
	private final ChampionDataHandler championDataHandler;

	public void syncChampionsFromApi() {
		log.info("🔄 챔피언 데이터 동기화 시작...");

		try {
			// 1단계: 최신 버전 조회
			List<String> versions = riotDataDragonApi.getVersions();
			String latestVersion = versions.getFirst();
			log.info("📦 최신 버전: {}", latestVersion);

			// 2단계: 챔피언 전체 데이터 조회
			ChampionData response = riotDataDragonApi.getChampionData(latestVersion);
			Map<String, ChampionBasicInfo> championMap = response.data();
			log.info("📊 조회된 챔피언 수: {}", championMap.size());

			// 3단계: Map 데이터를 DB에 저장
			int newCount = 0;
			int updatedCount = 0;

			for (ChampionBasicInfo championInfo : championMap.values()) {
				String championKey = championInfo.key();
				String championName = championInfo.name();

				if (championDataHandler.existsChampion(championKey)) {
					// 기존 챔피언 업데이트 (이름이 변경되었을 수도 있음)
					championDataHandler.updateChampion(championKey, championName);
					updatedCount++;
				} else {
					// 새로운 챔피언 추가
					championDataHandler.createChampion(championKey, championName);
					newCount++;
				}
			}

			log.info("✅ 챔피언 데이터 동기화 완료: 신규 {}개, 업데이트 {}개, 버전: {}",
				newCount, updatedCount, latestVersion);

		} catch (Exception e) {
			log.error("❌ 챔피언 데이터 동기화 실패: {}", e.getMessage());
			throw e;
		}
	}
}
