package project.lolmonitor.application.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.service.riot.ChampionService;
import project.lolmonitor.service.riot.GameStatusService;
import project.lolmonitor.service.riot.RiotUserService;
import project.lolmonitor.service.riot.SummonerLevelService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/riot")
public class RiotController {

	private final RiotUserService riotUserService;
	private final GameStatusService gameStatusService;
	private final ChampionService championService;
	private final SummonerLevelService summonerLevelService;

	/**
	 * 라이엇 유저 DB에 추가 및 현재 게임 중 상태 확인
	 */
	@GetMapping
	public ResponseEntity<String> checkGameStatus(@RequestParam String gameNickname, @RequestParam String tagLine) {
		gameStatusService.checkGameStatus(gameNickname, tagLine);
		return ResponseEntity.ok("✅ " + gameNickname + "#" + tagLine + " 상태 확인 완료");
	}

	/**
	 * 레벨 업 확인
	 */
	@GetMapping("/level-up")
	public ResponseEntity<String> checkLevelUp(@RequestParam String gameNickname, @RequestParam String tagLine) {
		RiotUser riotUser = riotUserService.getRiotUser(gameNickname, tagLine);
		summonerLevelService.checkSummonerLevel(riotUser);
		return ResponseEntity.ok("✅ " + gameNickname + "#" + tagLine + " 상태 확인 완료");
	}

	/**
	 * 사용자 모니터링 활성화
	 */
	@PostMapping("/enable")
	public ResponseEntity<Void> enableMonitoring(
		@RequestParam String gameNickname,
		@RequestParam String tagLine) {

		riotUserService.enableRiotUserMonitoring(gameNickname, tagLine);
		return ResponseEntity.ok().build();
	}

	/**
	 * 사용자 모니터링 비활성화
	 */
	@PostMapping("/disable")
	public ResponseEntity<Void> disableMonitoring(
		@RequestParam String gameNickname,
		@RequestParam String tagLine) {

		riotUserService.disableRiotUserMonitoring(gameNickname, tagLine);
		return ResponseEntity.ok().build();
	}

	/**
	 * 챔피언 데이터 동기화 실행
	 */
	@PostMapping("/sync")
	public ResponseEntity<Map<String, Object>> syncChampions() {
		try {
			championService.syncChampionsFromApi();

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("message", "챔피언 데이터 동기화 완료");result.put("timestamp", LocalDateTime.now());

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("success", false);
			error.put("message", "동기화 실패: " + e.getMessage());

			return ResponseEntity.status(500).body(error);
		}
	}
}
