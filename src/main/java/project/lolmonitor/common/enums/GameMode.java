package project.lolmonitor.common.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameMode {
	CLASSIC("CLASSIC", "소환사의 협곡"),
	ARAM("ARAM", "무작위 총력전"),
	URF("URF", "우르프"),
	ONEFORALL("ONEFORALL", "원 포 올"),
	NEXUSBLITZ("NEXUSBLITZ", "넥서스 블리츠"),
	CHERRY("CHERRY", "아레나"),
	UNKNOWN("UNKNOWN", "알 수 없는 모드");

	private final String code;
	private final String koreanName;

	/**
	 * 코드로 GameMode 찾기
	 */
	public static GameMode fromCode(String code) {
		if (code == null) {
			return UNKNOWN;
		}

		return Arrays.stream(values())
					 .filter(mode -> mode.code.equals(code))
					 .findFirst()
					 .orElse(UNKNOWN);
	}

	/**
	 * 한국어 이름 반환 (null safe)
	 */
	public static String getKoreanName(String code) {
		return fromCode(code).getKoreanName();
	}
}
