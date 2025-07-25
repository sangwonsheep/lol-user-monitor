package project.lolmonitor.service.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.RiotUserDataHandler;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.service.riot.GameStatusService;
import project.lolmonitor.service.riot.SummonerLevelService;
import project.lolmonitor.service.statistics.StatisticsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotMonitorScheduler {

	private final GameStatusService gameStatusService;
	private final SummonerLevelService summonerLevelService;
	private final StatisticsService statisticsService;
	private final RiotUserDataHandler riotUserDataHandler;

	/**
	 * 주기적으로 모니터링 대상 플레이어 게임 상태 확인 (30초마다)
	 */
	@Scheduled(fixedDelayString = "30000")
	public void checkAllPlayers() {
		List<RiotUser> riotUsers = riotUserDataHandler.getMonitorRiotUsers();

		if (riotUsers.isEmpty()) {
			log.debug("모니터링 대상 사용자가 없습니다.");
			return;
		}

		log.info("🕐 게임 상태 모니터링 시작 - 대상: {}명", riotUsers.size());

		for (RiotUser user : riotUsers) {
			try {
				gameStatusService.checkGameStatus(user.getGameNickname(), user.getTagLine());
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				log.error("❌ {} 게임 상태 모니터링 실패: {}", user.getDisplayName(), e.getMessage());
			}
		}
	}

	/**
	 * 주기적으로 모니터링 대상 플레이어 레벨 확인 (5분마다)
	 */
	@Scheduled(fixedDelayString = "300000") // 5분 = 300,000ms
	public void checkAllPlayersLevel() {
		List<RiotUser> riotUsers = riotUserDataHandler.getMonitorRiotUsers();

		if (riotUsers.isEmpty()) {
			log.debug("레벨 모니터링 대상 사용자가 없습니다.");
			return;
		}

		log.info("📊 레벨 모니터링 시작 - 대상: {}명", riotUsers.size());

		for (RiotUser user : riotUsers) {
			try {
				summonerLevelService.checkSummonerLevel(user);
				Thread.sleep(3000); // 레벨 체크는 조금 더 여유있게
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				log.error("❌ {} 레벨 모니터링 실패: {}", user.getDisplayName(), e.getMessage());
			}
		}
	}

	/**
	 * 매일 08:30에 일일 게임 통계 전송
	 */
	@Scheduled(cron = "0 30 8 * * *") // 매일 08:30:00
	public void sendDailyStatistics() {
		log.info("📊 일일 게임 통계 스케줄 시작");
		statisticsService.sendDailyGameStatistics();
	}

	/**
	 * 매주 월요일 09시에 주간 게임 통계 전송
	 * (지난 주 월요일 09:00 ~ 월요일 09:00)
	 */
	@Scheduled(cron = "0 0 9 * * MON")
	public void sendWeeklyStatistics() {
		log.info("📈 주간 게임 통계 스케줄 시작");
		statisticsService.sendWeeklyGameStatistics();
	}

	/**
	 * 매월 1일 09시 30분에 월간 게임 통계 전송
	 * (지난 달 1일 09:00 ~ 이번 달 1일 09:00)
	 */
	@Scheduled(cron = "0 30 9 1 * *")
	public void sendMonthlyStatistics() {
		log.info("📊 월간 게임 통계 스케줄 시작");
		statisticsService.sendMonthlyGameStatistics();
	}
}
