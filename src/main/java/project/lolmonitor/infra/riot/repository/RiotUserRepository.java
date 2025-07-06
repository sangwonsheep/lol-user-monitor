package project.lolmonitor.infra.riot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.lolmonitor.infra.riot.entity.RiotUser;

public interface RiotUserRepository extends JpaRepository<RiotUser, Long> {

	Optional<RiotUser> findByGameNicknameAndTagLine(String gameNickname, String tagLine);

	Optional<RiotUser> findByPuuid(String puuid);

	boolean existsByGameNicknameAndTagLine(String gameNickname, String tagLine);

	List<RiotUser> findByIsMonitoredTrue();
}
