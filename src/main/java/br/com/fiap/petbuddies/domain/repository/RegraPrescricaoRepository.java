package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegraPrescricaoRepository extends JpaRepository<RegraPrescricaoEntity, Long> {

    // O motor lê as regras de uma prescrição na ordem assinada (IX_REGRA_PRESC_ORDEM).
    List<RegraPrescricaoEntity> findByPrescricaoIdOrderByOrdemAsc(Long prescricaoId);

    // Quantas regras assinadas usam cada condição da clínica; condição sem regra não aparece.
    @Query("SELECT r.condicaoClinica.id, COUNT(r) FROM RegraPrescricaoEntity r "
            + "WHERE r.condicaoClinica.clinica.id = :clinicaId GROUP BY r.condicaoClinica.id")
    List<Object[]> contarPorCondicaoDaClinica(@Param("clinicaId") Long clinicaId);

    boolean existsByCondicaoClinicaId(Long condicaoClinicaId);
}
