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
import java.util.Optional;

public interface ItemPlanoCuidadoRepository extends JpaRepository<ItemPlanoCuidadoEntity, Long> {

    List<ItemPlanoCuidadoEntity> findByPlanoIdOrderByDataAlvoAsc(Long planoId);

    List<ItemPlanoCuidadoEntity> findByPlanoIdAndStatus(Long planoId, StatusItem status);

    // A baixa do item de casa (ADR s3-24 §5, escrita pelo J6): o check-in
    // encontra o item do dia daquela prescricao para gravar o desfecho nele.
    // UX_ITEM_PRESC_DATA garante no maximo um item por prescricao por dia.
    Optional<ItemPlanoCuidadoEntity> findByPrescricaoIdAndDataAlvo(Long prescricaoId, LocalDate dataAlvo);

    // Reconstrói os desfechos de um check-in já persistido (GET) — só enxerga
    // os itens que existiam para receber a baixa, ao contrário da resposta do
    // POST, que inclui também prescrições avaliadas sem item (ver CheckinService).
    List<ItemPlanoCuidadoEntity> findByCheckinId(Long checkinId);

    List<ItemPlanoCuidadoEntity> findByDataAlvoBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusItem status);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId")
    Page<ItemPlanoCuidadoEntity> findEventosPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId AND e.tipo = :tipo AND e.status = :status AND e.dataAlvo < :data")
    List<ItemPlanoCuidadoEntity> findEventosVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("tipo") TipoCuidado tipo,
            @Param("status") StatusItem status, @Param("data") LocalDate data);
}
