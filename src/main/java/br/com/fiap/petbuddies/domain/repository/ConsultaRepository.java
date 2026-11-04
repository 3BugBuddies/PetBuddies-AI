package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import org.springframework.data.domain.Pageable;
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

    // O painel mostra o responsável do animal em cada consulta do dia.
    @Query("SELECT c FROM ConsultaEntity c JOIN FETCH c.animal a JOIN FETCH a.responsavel JOIN FETCH c.veterinario "
            + "WHERE c.dataHora >= :inicio AND c.dataHora < :fim ORDER BY c.dataHora")
    List<ConsultaEntity> findNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // A mais recente primeiro; o chamador limita com Pageable.
    @Query("SELECT c.veterinario.clinica.id FROM ConsultaEntity c WHERE c.animal.id = :animalId ORDER BY c.dataHora DESC")
    List<Long> findClinicaIdsPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    // A pontuação lê responsável e clínica fora do fechamento: tudo num JOIN, sem lazy.
    @Query("SELECT c FROM ConsultaEntity c JOIN FETCH c.animal a JOIN FETCH a.responsavel "
            + "JOIN FETCH c.veterinario v JOIN FETCH v.clinica WHERE c.id = :id")
    Optional<ConsultaEntity> findParaPontuacao(@Param("id") Long id);

    // Início do período que a pontuação avalia. Nulo na primeira consulta realizada do animal.
    @Query("SELECT MAX(c.dataHora) FROM ConsultaEntity c WHERE c.animal.id = :animalId "
            + "AND c.status = :status AND c.dataHora < :antes")
    LocalDateTime findDataUltimaComStatusAntes(
            @Param("animalId") Long animalId,
            @Param("status") StatusConsulta status,
            @Param("antes") LocalDateTime antes);

    boolean existsByAnimalId(Long animalId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByIdAndAnimalId(Long id, Long animalId);
}
