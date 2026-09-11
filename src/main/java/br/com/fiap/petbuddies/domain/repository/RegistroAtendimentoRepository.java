package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistroAtendimentoRepository extends JpaRepository<RegistroAtendimentoEntity, Long> {

    List<RegistroAtendimentoEntity> findByAnimalIdOrderByDataAtendimentoDesc(Long animalId);

    List<RegistroAtendimentoEntity> findByConsultaId(Long consultaId);

    // Traz animal, consulta, veterinario e clinica num JOIN — le-los fora desta chamada explode em LazyInitializationException (open-in-view desligado).
    @Query("SELECT r FROM RegistroAtendimentoEntity r "
            + "JOIN FETCH r.animal "
            + "JOIN FETCH r.consulta c "
            + "JOIN FETCH c.veterinario v "
            + "JOIN FETCH v.clinica "
            + "WHERE r.id = :id")
    Optional<RegistroAtendimentoEntity> buscarComAnimalEVeterinario(@Param("id") Long id);
}
