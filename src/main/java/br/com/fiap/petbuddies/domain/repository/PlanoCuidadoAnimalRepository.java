package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoAnimalEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlanoCuidadoAnimalRepository extends JpaRepository<PlanoCuidadoAnimalEntity, Long> {

    List<PlanoCuidadoAnimalEntity> findByPetNetApiAnimalId(Long petNetApiAnimalId);

    @Query("SELECT p FROM PlanoCuidadoAnimalEntity p WHERE p.petNetApiAnimalId = :animalId AND p.status = :status")
    Optional<PlanoCuidadoAnimalEntity> findPlanoPorAnimalEStatus(
            @Param("animalId") Long animalId, @Param("status") StatusPlano status);

    @Query("SELECT p FROM PlanoCuidadoAnimalEntity p WHERE p.petNetApiAnimalId = :animalId AND p.status = :status AND p.protocolo.categoria = :categoria")
    Optional<PlanoCuidadoAnimalEntity> findPlanoAtivoPorCategoria(
            @Param("animalId") Long animalId, @Param("status") StatusPlano status, @Param("categoria") CategoriaProtocolo categoria);

    @Query("SELECT p FROM PlanoCuidadoAnimalEntity p WHERE p.petNetApiAnimalId = :animalId AND p.petNetApiConsultaId = :consultaId")
    Optional<PlanoCuidadoAnimalEntity> findPlanoPorAnimalEConsulta(
            @Param("animalId") Long animalId, @Param("consultaId") Long consultaId);

    List<PlanoCuidadoAnimalEntity> findByStatus(StatusPlano status);
}
