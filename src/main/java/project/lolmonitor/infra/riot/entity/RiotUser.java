package project.lolmonitor.infra.riot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.lolmonitor.infra.common.BaseEntity;

@Table(name = "riot_user")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiotUser extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "puuid", nullable = false, unique = true)
	private String puuid;

	@Column(name = "game_nickname", nullable = false)
	private String gameNickname; // 게임 닉네임 -> 게임관전중

	@Column(name = "tag_line", nullable = false)
	private String tagLine; // 닉네임 태그 -> #kr1

	@Column(name = "region", nullable = false)
	private String region;

	// 🆕 모니터링 대상 여부 플래그 추가
	@Column(name = "is_monitored", nullable = false)
	private boolean isMonitored;

	@Builder
	private RiotUser(String gameNickname, String tagLine, String puuid, String region, boolean isMonitored) {
		this.gameNickname = gameNickname;
		this.tagLine = tagLine;
		this.puuid = puuid;
		this.region = region;
		this.isMonitored = isMonitored;
	}

	public static RiotUser createRiotUser(String gameNickname, String tagLine, String puuid, String region) {
		return RiotUser
			.builder()
			.gameNickname(gameNickname)
			.tagLine(tagLine)
			.puuid(puuid)
			.region(region)
			.isMonitored(false)
			.build();
	}

	public void enableMonitoring() {
		this.isMonitored = true;
	}

	public void disableMonitoring() {
		this.isMonitored = false;
	}

	public String getDisplayName() {
		return gameNickname + "#" + tagLine;
	}
}
