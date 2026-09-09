package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ItemPlanoCuidadoRepository extends JpaRepository<ItemPlanoCuidadoEntity, Long> {

    List<ItemPlanoCuidadoEntity> findByPlanoIdOrderByDataAlvoAsc(Long planoId);

    List<ItemPlanoCuidadoEntity> findByPlanoIdAndStatus(Long planoId, StatusItem status);

    List<ItemPlanoCuidadoEntity> findByDataAlvoBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusItem status);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId")
    Page<ItemPlanoCuidadoEntity> findEventosPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId AND e.tipo = :tipo AND e.status = :status AND e.dataAlvo < :data")
    List<ItemPlanoCuidadoEntity> findEventosVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("tipo") TipoCuidado tipo,
            @Param("status") StatusItem status, @Param("data") LocalDate data);
}
