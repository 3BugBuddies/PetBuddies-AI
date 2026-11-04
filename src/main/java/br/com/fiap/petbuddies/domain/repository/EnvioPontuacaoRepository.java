package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.EnvioPontuacaoEntity;
import br.com.fiap.petbuddies.domain.enums.pontuacao.StatusEnvioPontuacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnvioPontuacaoRepository extends JpaRepository<EnvioPontuacaoEntity, Long> {

    Optional<EnvioPontuacaoEntity> findByConsultaId(Long consultaId);

    boolean existsByConsultaId(Long consultaId);

    @Query("SELECT e.consultaId FROM EnvioPontuacaoEntity e WHERE e.status = :status ORDER BY e.id")
    List<Long> findConsultaIdsPorStatus(@Param("status") StatusEnvioPontuacao status);
}
