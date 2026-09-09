package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescricaoRepository extends JpaRepository<PrescricaoEntity, Long> {

    List<PrescricaoEntity> findByAnimalIdOrderByDataInicioDesc(Long animalId);

    List<PrescricaoEntity> findByRegistroAtendimentoId(Long registroAtendimentoId);
}
