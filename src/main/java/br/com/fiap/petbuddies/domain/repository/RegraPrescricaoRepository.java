package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraPrescricaoRepository extends JpaRepository<RegraPrescricaoEntity, Long> {

    // O motor lê as regras de uma prescrição na ordem assinada (IX_REGRA_PRESC_ORDEM).
    List<RegraPrescricaoEntity> findByPrescricaoIdOrderByOrdemAsc(Long prescricaoId);
}
