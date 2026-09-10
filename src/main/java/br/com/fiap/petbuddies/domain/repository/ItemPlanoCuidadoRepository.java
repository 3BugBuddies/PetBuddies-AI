package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
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

    /**
     * Reforço vencido (PR-J9): itens de origem PROTOCOLO ainda em aberto cuja
     * data-alvo já passou — PENDENTE ou ATRASADO, os dois status que o CHECK do
     * item admite para "nao cumprido".
     */
    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId "
        + "AND e.origem = :origem AND e.status IN :statusVencidos AND e.dataAlvo < :hoje "
        + "ORDER BY e.dataAlvo ASC")
    List<ItemPlanoCuidadoEntity> findVencidosPorAnimal(
            @Param("animalId") Long animalId, @Param("origem") TipoOrigemItem origem,
            @Param("statusVencidos") List<StatusItem> statusVencidos, @Param("hoje") LocalDate hoje);

    /**
     * Todo o histórico do animal (qualquer plano, qualquer origem) para os tipos
     * de cuidado dados — a chave de "última realização" é o tipo, não a regra:
     * {@link br.com.fiap.petbuddies.domain.enums.TipoDataBase#ULTIMA_REALIZACAO}
     * é "a última vez que aquele cuidado foi feito", e a série de filhote e a
     * dose de manutenção são regras diferentes do mesmo tipo. Uma query para
     * todos os tipos de uma vez, para não repetir por regra.
     */
    @Query("SELECT e FROM ItemPlanoCuidadoEntity e WHERE e.plano.animalId = :animalId AND e.tipo IN :tipos")
    List<ItemPlanoCuidadoEntity> findHistoricoPorTipos(
            @Param("animalId") Long animalId, @Param("tipos") List<TipoCuidado> tipos);
}
