package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoEntity, Long> {

    List<ProcedimentoEntity> findByAnimalIdOrderByDataPrevistaInicioDesc(Long animalId);

    List<ProcedimentoEntity> findByRegistroAtendimentoId(Long registroAtendimentoId);
}
