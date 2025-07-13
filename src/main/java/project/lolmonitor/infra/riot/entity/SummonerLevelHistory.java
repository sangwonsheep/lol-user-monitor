package project.lolmonitor.infra.riot.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.lolmonitor.infra.common.BaseEntity;

// 소환사 레벨 이력 테이블
@Table(name = "summoner_level_history")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummonerLevelHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "riot_user_id", nullable = false)
	private RiotUser riotUser;

	@Column(name = "level", nullable = false)
	private int level;

	@Column(name = "level_up_time", nullable = false)
	private LocalDateTime levelUpTime;

	@Column(name = "games_played_for_levelup", nullable = false)
	private int gamesPlayedForLevelup;

	@Column(name = "time_taken_hours", nullable = false)
	private BigDecimal timeTakenHours;

	@Builder
	private SummonerLevelHistory(RiotUser riotUser, int level, LocalDateTime levelUpTime,
		int gamesPlayedForLevelup, BigDecimal timeTakenHours) {
		this.riotUser = riotUser;
		this.level = level;
		this.levelUpTime = levelUpTime;
		this.gamesPlayedForLevelup = gamesPlayedForLevelup;
		this.timeTakenHours = timeTakenHours;
	}

	public static SummonerLevelHistory createLevelHistory(RiotUser riotUser, int level,
		LocalDateTime levelUpTime, int gamesPlayedForLevelup, BigDecimal timeTakenHours) {
		return SummonerLevelHistory.builder()
			.riotUser(riotUser)
			.level(level)
			.levelUpTime(levelUpTime)
			.gamesPlayedForLevelup(gamesPlayedForLevelup)
			.timeTakenHours(timeTakenHours)
			.build();
	}
}