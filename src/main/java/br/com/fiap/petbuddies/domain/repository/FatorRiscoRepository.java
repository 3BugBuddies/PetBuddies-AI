package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.FatorRiscoEntity;
import br.com.fiap.petbuddies.domain.entity.ScoreRiscoAnimalEntity;
import br.com.fiap.petbuddies.domain.enums.TipoRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FatorRiscoRepository extends JpaRepository<FatorRiscoEntity, Long> {

    List<FatorRiscoEntity> findByScoreRiscoId(Long scoreRiscoId);

    Optional<FatorRiscoEntity> findByScoreRiscoIdAndTipo(Long scoreRiscoId, TipoRisco tipo);

    List<FatorRiscoEntity> findByScoreRisco(ScoreRiscoAnimalEntity scoreRisco);
}
