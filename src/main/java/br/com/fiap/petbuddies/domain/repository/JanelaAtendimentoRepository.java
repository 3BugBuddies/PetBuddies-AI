package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JanelaAtendimentoRepository extends JpaRepository<JanelaAtendimentoEntity, Long> {

    List<JanelaAtendimentoEntity> findByVeterinarioIdOrderByDataHoraInicioAsc(Long veterinarioId);

    // UK_JANELA_VET_INICIO
    boolean existsByVeterinarioIdAndDataHoraInicio(Long veterinarioId, LocalDateTime dataHoraInicio);

    boolean existsByVeterinarioIdAndDataHoraInicioAndIdNot(Long veterinarioId, LocalDateTime dataHoraInicio, Long id);
}
