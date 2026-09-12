package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {

    List<AnimalEntity> findByResponsavelId(Long responsavelId);

    List<AnimalEntity> findByNomeContainingIgnoreCase(String nome);

    // O JOIN FETCH e o que evita uma consulta por tutor ao montar o painel:
    // responsavel e LAZY e o nome dele aparece em toda linha da lista.
    @Query("SELECT a FROM AnimalEntity a JOIN FETCH a.responsavel WHERE a.id IN :ids")
    List<AnimalEntity> findComResponsavelPorIds(@Param("ids") Collection<Long> ids);
}
