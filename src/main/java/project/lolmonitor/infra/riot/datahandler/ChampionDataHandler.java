package project.lolmonitor.infra.riot.datahandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.entity.Champion;
import project.lolmonitor.infra.riot.repository.ChampionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChampionDataHandler {

	private final ChampionRepository championRepository;

	@Transactional(readOnly = true)
	public String getChampionName(String championKey) {
		Champion champion = championRepository.findByChampionKey(championKey)
											  .orElseThrow(() -> new RuntimeException("챔피언을 찾을 수 없습니다. : " + championKey));
		return champion.getChampionName();
	}

	@Transactional(readOnly = true)
	public boolean existsChampion(String championKey) {
		return championRepository.existsByChampionKey(championKey);
	}

	@Transactional
	public void createChampion(String championKey, String championName) {
		if (championRepository.existsByChampionKey(championKey)) {
			log.info("이미 존재하는 챔피언 : {}, championKey : {}", championName, championKey);
			return;
		}

		Champion champion = Champion.createChampion(championKey, championName);
		championRepository.save(champion);
	}

	@Transactional
	public void updateChampion(String championKey, String championName) {
		Champion champion = championRepository.findByChampionKey(championKey)
											  .orElseThrow(() -> new RuntimeException("챔피언을 찾을 수 없습니다. : " + championKey));
		champion.updateChampion(championKey, championName);
	}
}
