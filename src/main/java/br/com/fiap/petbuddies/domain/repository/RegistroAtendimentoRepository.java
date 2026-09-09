package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAtendimentoRepository extends JpaRepository<RegistroAtendimentoEntity, Long> {

    List<RegistroAtendimentoEntity> findByAnimalIdOrderByDataAtendimentoDesc(Long animalId);

    List<RegistroAtendimentoEntity> findByConsultaId(Long consultaId);
}
