package project.lolmonitor.infra.riot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.lolmonitor.infra.riot.entity.RiotUser;
import project.lolmonitor.infra.riot.entity.SummonerLevelHistory;

public interface SummonerLevelHistoryRepository extends JpaRepository<SummonerLevelHistory, Long> {

	Optional<SummonerLevelHistory> findTopByRiotUserOrderByLevelUpTimeDesc(RiotUser riotUser);

	List<SummonerLevelHistory> findByRiotUserOrderByLevelUpTimeDesc(RiotUser riotUser);
}
