package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CondicaoObservadaRepository extends JpaRepository<CondicaoObservadaEntity, Long> {

    List<CondicaoObservadaEntity> findByCheckin_Id(Long checkinId);
}
