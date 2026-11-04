package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PrescricaoRepository extends JpaRepository<PrescricaoEntity, Long> {

    List<PrescricaoEntity> findByAnimalIdOrderByDataInicioDesc(Long animalId);

    List<PrescricaoEntity> findByRegistroAtendimentoId(Long registroAtendimentoId);

    @Query("SELECT p FROM PrescricaoEntity p JOIN FETCH p.veterinario v JOIN FETCH v.clinica WHERE p.id IN :ids")
    List<PrescricaoEntity> findComPrescritorPorIds(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT p.veterinario.clinica.id FROM PrescricaoEntity p WHERE p.id IN :ids")
    List<Long> findClinicaIdsPorPrescricoes(@Param("ids") Collection<Long> ids);

    boolean existsByAnimalId(Long animalId);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByRegistroAtendimentoId(Long registroAtendimentoId);
}
