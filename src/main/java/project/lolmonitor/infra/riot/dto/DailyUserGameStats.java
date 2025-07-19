package project.lolmonitor.infra.riot.dto;

public record DailyUserGameStats(
	String playerName,
	int totalGames
) {}
