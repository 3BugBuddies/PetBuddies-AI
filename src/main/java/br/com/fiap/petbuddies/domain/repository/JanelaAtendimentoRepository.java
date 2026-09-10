package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JanelaAtendimentoRepository extends JpaRepository<JanelaAtendimentoEntity, Long> {

    List<JanelaAtendimentoEntity> findByVeterinarioIdOrderByDataHoraInicioAsc(Long veterinarioId);

    // UK_JANELA_VET_INICIO
    boolean existsByVeterinarioIdAndDataHoraInicio(Long veterinarioId, LocalDateTime dataHoraInicio);

    boolean existsByVeterinarioIdAndDataHoraInicioAndIdNot(Long veterinarioId, LocalDateTime dataHoraInicio, Long id);

    // agenda do dia: só os slots sem consulta vinculada
    List<JanelaAtendimentoEntity> findByVeterinarioIdAndConsultaIsNullAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(
            Long veterinarioId, LocalDateTime inicio, LocalDateTime fim);

    // devolve o slot ao cancelar — FK_JANELA_CONSULTA é ON DELETE SET NULL e cancelamento não é exclusão
    Optional<JanelaAtendimentoEntity> findByConsultaId(Long consultaId);
}
