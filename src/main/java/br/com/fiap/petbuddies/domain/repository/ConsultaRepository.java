package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    List<ConsultaEntity> findByAnimalIdOrderByDataHoraDesc(Long animalId);

    List<ConsultaEntity> findByVeterinarioIdOrderByDataHoraDesc(Long veterinarioId);

    @Query("SELECT c FROM ConsultaEntity c WHERE c.dataHora >= :inicio AND c.dataHora < :fim ORDER BY c.dataHora")
    List<ConsultaEntity> findNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
