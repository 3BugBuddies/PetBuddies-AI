package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.CheckinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<CheckinEntity, Long> {

    List<CheckinEntity> findByAnimal_IdOrderByDataReferenciaDesc(Long animalId);

    // UK_CHECKIN_ANIMAL_DIA e chave parcialmente nula (ID_ITEM_PLANO_CUIDADO
    // aceita nulo). "= :itemId" nunca casa com nulo em JPQL, por isso o OR
    // trata os dois casos: relato geral (itemId nulo) e relato por item.
    @Query("SELECT c FROM CheckinEntity c WHERE c.animal.id = :animalId AND c.dataReferencia = :dataReferencia "
            + "AND ((:itemId IS NULL AND c.itemPlanoCuidado IS NULL) OR c.itemPlanoCuidado.id = :itemId)")
    Optional<CheckinEntity> findExistente(
            @Param("animalId") Long animalId,
            @Param("dataReferencia") LocalDate dataReferencia,
            @Param("itemId") Long itemId);
}
