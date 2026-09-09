package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlanoCuidadoRepository extends JpaRepository<PlanoCuidadoEntity, Long> {

    List<PlanoCuidadoEntity> findByAnimalId(Long animalId);

    @Query("SELECT p FROM PlanoCuidadoEntity p WHERE p.animalId = :animalId AND p.status = :status")
    Optional<PlanoCuidadoEntity> findPlanoPorAnimalEStatus(
            @Param("animalId") Long animalId, @Param("status") StatusPlano status);

    @Query("SELECT p FROM PlanoCuidadoEntity p WHERE p.animalId = :animalId AND p.status = :status AND p.categoria = :categoria")
    Optional<PlanoCuidadoEntity> findPlanoAtivoPorCategoria(
            @Param("animalId") Long animalId, @Param("status") StatusPlano status, @Param("categoria") CategoriaPlano categoria);

    @Query("SELECT p FROM PlanoCuidadoEntity p WHERE p.animalId = :animalId AND p.consultaId = :consultaId")
    Optional<PlanoCuidadoEntity> findPlanoPorAnimalEConsulta(
            @Param("animalId") Long animalId, @Param("consultaId") Long consultaId);

    List<PlanoCuidadoEntity> findByStatus(StatusPlano status);
}
