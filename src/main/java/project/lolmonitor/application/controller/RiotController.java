package project.lolmonitor.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.lolmonitor.service.riot.RiotService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/riot")
public class RiotController {

	private final RiotService riotService;

	/**
	 * 라이엇 유저 DB에 추가 및 현재 게임 중 상태 확인
	 */
	@GetMapping
	public ResponseEntity<String> checkGameStatus(@RequestParam String gameNickName, @RequestParam String tagLine) {
		riotService.checkGameStatus(gameNickName, tagLine);
		return ResponseEntity.ok("✅ " + gameNickName + "#" + tagLine + " 상태 확인 완료");
	}

	/**
	 * 사용자 모니터링 활성화
	 */
	@PostMapping("/enable")
	public ResponseEntity<Void> enableMonitoring(
		@RequestParam String gameNickname,
		@RequestParam String tagLine) {

		riotService.enableRiotUserMonitoring(gameNickname, tagLine);
		return ResponseEntity.ok().build();
	}

	/**
	 * 사용자 모니터링 비활성화
	 */
	@PostMapping("/disable")
	public ResponseEntity<Void> disableMonitoring(
		@RequestParam String gameNickname,
		@RequestParam String tagLine) {

		riotService.disableRiotUserMonitoring(gameNickname, tagLine);
		return ResponseEntity.ok().build();
	}
}
