package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.RegraProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegraProtocoloRepository extends JpaRepository<RegraProtocoloEntity, Long> {

    List<RegraProtocoloEntity> findByProtocoloId(Long protocoloId);

    List<RegraProtocoloEntity> findByProtocoloIdAndTipo(Long protocoloId, TipoCuidado tipo);
}
