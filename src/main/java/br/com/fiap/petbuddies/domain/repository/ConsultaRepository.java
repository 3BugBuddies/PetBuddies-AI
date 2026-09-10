package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    List<ConsultaEntity> findByAnimalIdOrderByDataHoraDesc(Long animalId);

    List<ConsultaEntity> findByVeterinarioIdOrderByDataHoraDesc(Long veterinarioId);
}
