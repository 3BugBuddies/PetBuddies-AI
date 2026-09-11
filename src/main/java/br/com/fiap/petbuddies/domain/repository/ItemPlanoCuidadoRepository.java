package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
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

    // O check-in encontra o item do dia daquela prescricao para gravar o desfecho. UX_ITEM_PRESC_DATA garante no maximo um item por prescricao por dia.
    Optional<ItemPlanoCuidadoEntity> findByPrescricaoIdAndDataAlvo(Long prescricaoId, LocalDate dataAlvo);

    // So enxerga os itens que existiam para receber a baixa — a resposta do POST inclui tambem prescricoes avaliadas sem item.
    List<ItemPlanoCuidadoEntity> findByCheckinId(Long checkinId);

    List<ItemPlanoCuidadoEntity> findByDataAlvoBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusItem status);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId")
    Page<ItemPlanoCuidadoEntity> findEventosPorAnimal(@Param("animalId") Long animalId, Pageable pageable);

    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId AND e.tipo = :tipo AND e.status = :status AND e.dataAlvo < :data")
    List<ItemPlanoCuidadoEntity> findEventosVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("tipo") TipoCuidado tipo,
            @Param("status") StatusItem status, @Param("data") LocalDate data);

    // Itens de origem PROTOCOLO ainda em aberto cuja data-alvo ja passou — PENDENTE ou ATRASADO, os dois status que o CHECK do item admite para "nao cumprido".
    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId "
        + "AND e.origem = :origem AND e.status IN :statusVencidos AND e.dataAlvo < :hoje "
        + "ORDER BY e.dataAlvo ASC")
    List<ItemPlanoCuidadoEntity> findVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("origem") TipoOrigemItem origem,
            @Param("statusVencidos") List<StatusItem> statusVencidos, @Param("hoje") LocalDate hoje);

    // Chave de "ultima realizacao" e o tipo, nao a regra — uma query para todos os tipos de uma vez, sem repetir por regra.
    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId AND e.tipo IN :tipos")
    List<ItemPlanoCuidadoEntity> findHistoricoPorTipos(
            @Param("animalId") Long animalId, @Param("tipos") List<TipoCuidado> tipos);
}
