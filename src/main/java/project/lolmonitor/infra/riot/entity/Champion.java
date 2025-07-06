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

@Table(name = "champion")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Champion extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "champion_key", nullable = false, unique = true)
	private String championKey;

	@Column(name = "champion_name")
	private String championName;

	@Builder
	private Champion(String championKey, String championName) {
		this.championKey = championKey;
		this.championName = championName;
	}

	public static Champion createChampion(String championKey, String championName) {
		return Champion.builder()
			.championKey(championKey)
			.championName(championName)
			.build();
	}

	public void updateChampion(String championKey, String championName) {
		this.championKey = championKey;
		this.championName = championName;
	}
}
