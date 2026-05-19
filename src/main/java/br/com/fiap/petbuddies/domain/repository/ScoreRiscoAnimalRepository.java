package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ScoreRiscoAnimalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ScoreRiscoAnimalRepository extends JpaRepository<ScoreRiscoAnimalEntity, Long> {

    @Query("SELECT s FROM ScoreRiscoAnimalEntity s WHERE s.petNetApiAnimalId = :animalId ORDER BY s.calculadoEm DESC")
    List<ScoreRiscoAnimalEntity> findScoresPorAnimal(@Param("animalId") Long animalId);

    @Query(value = "SELECT s FROM ScoreRiscoAnimalEntity s WHERE s.petNetApiAnimalId = :animalId ORDER BY s.calculadoEm DESC",
           countQuery = "SELECT COUNT(s) FROM ScoreRiscoAnimalEntity s WHERE s.petNetApiAnimalId = :animalId")
    Page<ScoreRiscoAnimalEntity> findScoresPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    @Query("SELECT s FROM ScoreRiscoAnimalEntity s WHERE s.petNetApiAnimalId = :animalId ORDER BY s.calculadoEm DESC LIMIT 1")
    Optional<ScoreRiscoAnimalEntity> findScoreMaisRecente(@Param("animalId") Long animalId);

    boolean existsByPetNetApiAnimalId(Long petNetApiAnimalId);
}
