package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoAnimalEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanoCuidadoAnimalRepository extends JpaRepository<PlanoCuidadoAnimalEntity, Long> {

    List<PlanoCuidadoAnimalEntity> findByPetNetApiAnimalId(Long petNetApiAnimalId);

    Optional<PlanoCuidadoAnimalEntity> findByPetNetApiAnimalIdAndStatus(Long petNetApiAnimalId, StatusPlano status);

    Optional<PlanoCuidadoAnimalEntity> findByPetNetApiAnimalIdAndStatusAndProtocolo_Categoria(
            Long petNetApiAnimalId, StatusPlano status, CategoriaProtocolo categoria);

    Optional<PlanoCuidadoAnimalEntity> findByPetNetApiAnimalIdAndPetNetApiConsultaId(
            Long petNetApiAnimalId, Long petNetApiConsultaId);

    List<PlanoCuidadoAnimalEntity> findByStatus(StatusPlano status);
}
