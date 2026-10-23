package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {

    @EntityGraph(attributePaths = "responsavel")
    List<AnimalEntity> findByResponsavelId(Long responsavelId);

    @EntityGraph(attributePaths = "responsavel")
    List<AnimalEntity> findByNomeContainingIgnoreCase(String nome);

    // AnimalResponse lê o responsável fora da transação (open-in-view desligado); sem o fetch, a lista vira N+1 ou LazyInitializationException.
    @Query("SELECT a FROM AnimalEntity a JOIN FETCH a.responsavel")
    List<AnimalEntity> findAllComResponsavel();

    @Query("SELECT a FROM AnimalEntity a JOIN FETCH a.responsavel WHERE a.id = :id")
    Optional<AnimalEntity> findComResponsavelById(@Param("id") Long id);

    // O JOIN FETCH e o que evita uma consulta por tutor ao montar o painel:
    // responsavel e LAZY e o nome dele aparece em toda linha da lista.
    @Query("SELECT a FROM AnimalEntity a JOIN FETCH a.responsavel WHERE a.id IN :ids")
    List<AnimalEntity> findComResponsavelPorIds(@Param("ids") Collection<Long> ids);

    boolean existsByResponsavelId(Long responsavelId);
}
