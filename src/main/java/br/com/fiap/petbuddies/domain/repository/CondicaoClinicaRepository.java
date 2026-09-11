package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CondicaoClinicaRepository extends JpaRepository<CondicaoClinicaEntity, Long> {

    List<CondicaoClinicaEntity> findByClinicaId(Long clinicaId);

    Optional<CondicaoClinicaEntity> findByClinicaIdAndCodigo(Long clinicaId, String codigo);

    // UK_CONDICAO_CLINICA_CODIGO: o código repete entre clínicas, nunca dentro de uma.
    boolean existsByClinicaIdAndCodigo(Long clinicaId, String codigo);

    boolean existsByClinicaIdAndCodigoAndIdNot(Long clinicaId, String codigo, Long id);

    // Escala independente de regra de prescricao — entra no prompt de todo check-in, nao so dos animais com prescricao ativa.
    List<CondicaoClinicaEntity> findByCriticaTrueAndAtivoTrue();
}
