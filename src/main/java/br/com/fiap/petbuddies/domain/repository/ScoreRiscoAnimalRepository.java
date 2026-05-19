package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ScoreRiscoAnimalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScoreRiscoAnimalRepository extends JpaRepository<ScoreRiscoAnimalEntity, Long> {

    List<ScoreRiscoAnimalEntity> findByPetNetApiAnimalIdOrderByCalculadoEmDesc(Long petNetApiAnimalId);

    Page<ScoreRiscoAnimalEntity> findByPetNetApiAnimalIdOrderByCalculadoEmDesc(Long petNetApiAnimalId, Pageable pageable);

    Optional<ScoreRiscoAnimalEntity> findFirstByPetNetApiAnimalIdOrderByCalculadoEmDesc(Long petNetApiAnimalId);

    boolean existsByPetNetApiAnimalId(Long petNetApiAnimalId);
}
