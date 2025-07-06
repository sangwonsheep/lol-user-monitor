package project.lolmonitor.service.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.RiotUserDataHandler;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.service.riot.RiotService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotMonitorScheduler {

	private final RiotService riotService;
	private final RiotUserDataHandler riotUserDataHandler;

	/**
	 * 주기적으로 모니터링 대상 플레이어 게임 상태 확인
	 */
	@Scheduled(fixedDelayString = "30000")
	public void checkAllPlayers() {
		List<RiotUser> riotUsers = riotUserDataHandler.getMonitorRiotUsers();

		if (riotUsers.isEmpty()) {
			log.debug("모니터링 대상 사용자가 없습니다.");
			return;
		}

		log.info("🕐 정기 모니터링 시작 - 대상: {}명", riotUsers.size());

		int successCount = 0;
		int failCount = 0;

		for (RiotUser user : riotUsers) {
			try {
				riotService.checkGameStatus(user.getGameNickname(), user.getTagLine());
				successCount++;

				// API 호출 제한 방지
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("⚠️ 모니터링 중단됨");
				break;
			} catch (Exception e) {
				log.error("❌ {} 모니터링 실패: {}", user.getDisplayName(), e.getMessage());
				failCount++;
			}
		}

		log.info("✅ 정기 모니터링 완료 - 성공: {}명, 실패: {}명", successCount, failCount);
	}
}
