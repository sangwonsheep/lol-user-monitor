package project.lolmonitor.service.riot;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.infra.riot.datahandler.RiotUserDataHandler;
import project.lolmonitor.infra.riot.entity.RiotUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiotUserService {

	private final RiotUserDataHandler riotUserDataHandler;

	public RiotUser getRiotUser(String gameNickName, String tagLine) {
		return riotUserDataHandler.getRiotUser(gameNickName, tagLine);
	}

	public void enableRiotUserMonitoring(String gameNickName, String tagLine) {
		riotUserDataHandler.enableRiotUserMonitoring(gameNickName, tagLine);
	}

	public void disableRiotUserMonitoring(String gameNickName, String tagLine) {
		riotUserDataHandler.disableRiotUserMonitoring(gameNickName, tagLine);
	}
}
