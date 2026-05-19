package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface EventoPlanoRepository extends JpaRepository<EventoPlanoEntity, Long> {

    List<EventoPlanoEntity> findByPlanoIdOrderByDataAlvoAsc(Long planoId);

    List<EventoPlanoEntity> findByPlanoIdAndStatus(Long planoId, StatusEventoPlano status);

    List<EventoPlanoEntity> findByDataAlvoBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusEventoPlano status);

    @Query("SELECT e FROM EventoPlanoEntity e WHERE e.plano.petNetApiAnimalId = :animalId")
    Page<EventoPlanoEntity> findEventosPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    @Query("SELECT e FROM EventoPlanoEntity e WHERE e.plano.petNetApiAnimalId = :animalId AND e.tipo = :tipo AND e.status = :status AND e.dataAlvo < :data")
    List<EventoPlanoEntity> findEventosVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("tipo") TipoEventoProtocolo tipo,
            @Param("status") StatusEventoPlano status, @Param("data") LocalDate data);
}
