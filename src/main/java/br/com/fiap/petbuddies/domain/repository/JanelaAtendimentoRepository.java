package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JanelaAtendimentoRepository extends JpaRepository<JanelaAtendimentoEntity, Long> {

    List<JanelaAtendimentoEntity> findByVeterinarioIdOrderByDataHoraInicioAsc(Long veterinarioId);

    // UK_JANELA_VET_INICIO
    boolean existsByVeterinarioIdAndDataHoraInicio(Long veterinarioId, LocalDateTime dataHoraInicio);

    boolean existsByVeterinarioIdAndDataHoraInicioAndIdNot(Long veterinarioId, LocalDateTime dataHoraInicio, Long id);

    // "< :fim", e nao BETWEEN: BETWEEN inclui a meia-noite do dia seguinte
    @Query("SELECT j FROM JanelaAtendimentoEntity j WHERE j.veterinario.id = :veterinarioId AND j.consulta IS NULL"
            + " AND j.dataHoraInicio >= :inicio AND j.dataHoraInicio < :fim ORDER BY j.dataHoraInicio")
    List<JanelaAtendimentoEntity> findLivresNoPeriodo(
            @Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // A grade da semana mostra animal e responsável de cada consulta: sem o fetch, a leitura fora da transação quebra.
    @Query("SELECT j FROM JanelaAtendimentoEntity j LEFT JOIN FETCH j.consulta c LEFT JOIN FETCH c.animal a "
            + "LEFT JOIN FETCH a.responsavel WHERE j.veterinario.id = :veterinarioId "
            + "AND j.dataHoraInicio >= :inicio AND j.dataHoraInicio < :fim ORDER BY j.dataHoraInicio")
    List<JanelaAtendimentoEntity> findDaSemana(
            @Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT j FROM JanelaAtendimentoEntity j JOIN FETCH j.veterinario WHERE j.consulta IS NULL "
            + "AND j.dataHoraInicio >= :inicio AND j.dataHoraInicio < :fim ORDER BY j.dataHoraInicio")
    List<JanelaAtendimentoEntity> findLivresDaClinica(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Os inícios já abertos de um veterinário num período: o gerador pula estes (UK_JANELA_VET_INICIO).
    @Query("SELECT j.dataHoraInicio FROM JanelaAtendimentoEntity j WHERE j.veterinario.id = :veterinarioId "
            + "AND j.dataHoraInicio >= :inicio AND j.dataHoraInicio < :fim")
    List<LocalDateTime> findInicios(
            @Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // devolve o slot ao cancelar — FK_JANELA_CONSULTA é ON DELETE SET NULL e cancelamento não é exclusão
    Optional<JanelaAtendimentoEntity> findByConsultaId(Long consultaId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByConsultaId(Long consultaId);

    boolean existsByConsultaIdAndIdNot(Long consultaId, Long id);
}
