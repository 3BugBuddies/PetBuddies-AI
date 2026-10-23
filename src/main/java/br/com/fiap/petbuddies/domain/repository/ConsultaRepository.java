package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    List<ConsultaEntity> findByAnimalIdOrderByDataHoraDesc(Long animalId);

    List<ConsultaEntity> findByVeterinarioIdOrderByDataHoraDesc(Long veterinarioId);

    @Query("SELECT c FROM ConsultaEntity c WHERE c.dataHora >= :inicio AND c.dataHora < :fim ORDER BY c.dataHora")
    List<ConsultaEntity> findNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // A mais recente primeiro; o chamador limita com Pageable.
    @Query("SELECT c.veterinario.clinica.id FROM ConsultaEntity c WHERE c.animal.id = :animalId ORDER BY c.dataHora DESC")
    List<Long> findClinicaIdsPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    boolean existsByAnimalId(Long animalId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByIdAndAnimalId(Long id, Long animalId);
}
