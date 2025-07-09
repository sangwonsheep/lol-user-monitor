package project.lolmonitor.infra.riot.datahandler;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.repository.RiotUserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotUserDataHandler {

	private final RiotUserRepository riotUserRepository;

	@Transactional(readOnly = true)
	public RiotUser getRiotUser(String gameNickname, String tagLine) {
		return riotUserRepository.findByGameNicknameAndTagLine(gameNickname, tagLine)
											  .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
	}

	@Transactional(readOnly = true)
	public List<RiotUser> getMonitorRiotUsers() {
		return riotUserRepository.findByIsMonitoredTrue();
	}

	@Transactional
	public RiotUser createRiotUser(String gameNickname, String tagLine, String puuid) {
		RiotUser riotUser = RiotUser.createRiotUser(gameNickname, tagLine, puuid, "KR");
		return riotUserRepository.save(riotUser);
	}

	@Transactional(readOnly = true)
	public boolean existsRiotUser(String gameName, String tagLine) {
		return riotUserRepository.existsByGameNicknameAndTagLine(gameName, tagLine);
	}

	@Transactional
	public RiotUser enableRiotUserMonitoring(String gameNickname, String tagLine) {
		RiotUser riotUser = riotUserRepository.findByGameNicknameAndTagLine(gameNickname, tagLine)
										  .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자: " + gameNickname + "#" + tagLine));
		riotUser.enableMonitoring();
		RiotUser saved = riotUserRepository.save(riotUser);

		log.info("✅ 모니터링 활성화: {}", riotUser.getDisplayName());
		return saved;
	}

	@Transactional
	public RiotUser disableRiotUserMonitoring(String gameNickname, String tagLine) {
		RiotUser riotUser = riotUserRepository.findByGameNicknameAndTagLine(gameNickname, tagLine)
											  .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자: " + gameNickname + "#" + tagLine));
		riotUser.disableMonitoring();
		RiotUser saved = riotUserRepository.save(riotUser);

		log.info("❌ 모니터링 비활성화: {}", riotUser.getDisplayName());
		return saved;
	}
}
