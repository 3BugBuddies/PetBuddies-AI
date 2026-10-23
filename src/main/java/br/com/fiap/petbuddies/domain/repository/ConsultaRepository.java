package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    @EntityGraph(attributePaths = {"animal", "veterinario"})
    List<ConsultaEntity> findByAnimalIdOrderByDataHoraDesc(Long animalId);

    @EntityGraph(attributePaths = {"animal", "veterinario"})
    List<ConsultaEntity> findByVeterinarioIdOrderByDataHoraDesc(Long veterinarioId);

    // ConsultaResponse lê os nomes fora da transação (open-in-view desligado); o fetch evita N+1 na lista.
    @Query("SELECT c FROM ConsultaEntity c JOIN FETCH c.animal JOIN FETCH c.veterinario")
    List<ConsultaEntity> findAllComAnimalEVeterinario();

    @Query("SELECT c FROM ConsultaEntity c JOIN FETCH c.animal JOIN FETCH c.veterinario WHERE c.id = :id")
    Optional<ConsultaEntity> findComAnimalEVeterinarioById(@Param("id") Long id);

    @Query("SELECT c FROM ConsultaEntity c JOIN FETCH c.animal JOIN FETCH c.veterinario "
            + "WHERE c.dataHora >= :inicio AND c.dataHora < :fim ORDER BY c.dataHora")
    List<ConsultaEntity> findNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    boolean existsByAnimalId(Long animalId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByIdAndAnimalId(Long id, Long animalId);
}
