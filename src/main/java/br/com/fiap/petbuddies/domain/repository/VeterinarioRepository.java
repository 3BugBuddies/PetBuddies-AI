package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<VeterinarioEntity, Long> {

    Optional<VeterinarioEntity> findByCrmv(String crmv);

    // UK_VETERINARIO_CRMV
    boolean existsByCrmv(String crmv);

    boolean existsByCrmvAndIdNot(String crmv, Long id);

    List<VeterinarioEntity> findByClinicaId(Long clinicaId);

    // Projeção: só o id é lido, sem carregar a clínica.
    @Query("select v.clinica.id from VeterinarioEntity v where v.id = :id")
    Optional<Long> findClinicaIdById(@Param("id") Long id);
}
