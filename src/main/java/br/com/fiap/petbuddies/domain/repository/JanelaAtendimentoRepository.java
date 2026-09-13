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

    // devolve o slot ao cancelar — FK_JANELA_CONSULTA é ON DELETE SET NULL e cancelamento não é exclusão
    Optional<JanelaAtendimentoEntity> findByConsultaId(Long consultaId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByConsultaId(Long consultaId);

    boolean existsByConsultaIdAndIdNot(Long consultaId, Long id);
}
