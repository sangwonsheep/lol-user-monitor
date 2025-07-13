package project.lolmonitor.infra.riot.datahandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.entity.SummonerLevelHistory;
import project.lolmonitor.infra.riot.repository.SummonerLevelHistoryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummonerLevelHistoryDataHandler {

	private final SummonerLevelHistoryRepository levelHistoryRepository;

	/**
	 * 마지막 레벨업 이력 조회
	 */
	@Transactional(readOnly = true)
	public SummonerLevelHistory getLatestLevelHistory(RiotUser riotUser) {
		return levelHistoryRepository.findTopByRiotUserOrderByLevelUpTimeDesc(riotUser)
									 .orElseThrow(() -> new RuntimeException("존재하지 않는 레벨 히스토리"));
	}

	/**
	 * 마지막으로 기록된 레벨 조회
	 */
	@Transactional(readOnly = true)
	public int getLastRecordedLevel(RiotUser riotUser) {
		return levelHistoryRepository.findTopByRiotUserOrderByLevelUpTimeDesc(riotUser)
									 .map(SummonerLevelHistory::getLevel)
									 .orElse(0); // 첫 기록이면 0으로 시작
	}

	/**
	 * 마지막 레벨업 시간 조회
	 */
	@Transactional(readOnly = true)
	public LocalDateTime getLastLevelUpTime(RiotUser riotUser) {
		return levelHistoryRepository.findTopByRiotUserOrderByLevelUpTimeDesc(riotUser)
									 .map(SummonerLevelHistory::getLevelUpTime)
									 .orElse(riotUser.getCreatedAt()); // 첫 레벨업이면 유저 생성 시간부터
	}

	/**
	 * 레벨업 이력 저장
	 */
	@Transactional
	public SummonerLevelHistory saveLevelHistory(RiotUser riotUser, int level, LocalDateTime levelUpTime,
		int gamesPlayed, BigDecimal timeTaken) {
		SummonerLevelHistory levelHistory = SummonerLevelHistory
			.builder()
			.riotUser(riotUser)
			.level(level)
			.levelUpTime(levelUpTime)
			.gamesPlayedForLevelup(gamesPlayed)
			.timeTakenHours(timeTaken)
			.build();

		return levelHistoryRepository.save(levelHistory);
	}

	/**
	 * 특정 유저의 모든 레벨업 이력 조회 (최신순)
	 */
	@Transactional(readOnly = true)
	public List<SummonerLevelHistory> getAllLevelHistories(RiotUser riotUser) {
		return levelHistoryRepository.findByRiotUserOrderByLevelUpTimeDesc(riotUser);
	}
}
