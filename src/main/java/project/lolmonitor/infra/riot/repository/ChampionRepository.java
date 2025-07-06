package project.lolmonitor.infra.riot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.lolmonitor.infra.riot.entity.Champion;

public interface ChampionRepository extends JpaRepository<Champion, Long> {

	Optional<Champion> findByChampionKey(String championKey);

	boolean existsByChampionKey(String championKey);
}
