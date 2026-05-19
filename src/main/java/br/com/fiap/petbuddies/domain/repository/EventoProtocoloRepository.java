package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoProtocoloRepository extends JpaRepository<EventoProtocoloEntity, Long> {

    List<EventoProtocoloEntity> findByProtocoloId(Long protocoloId);

    List<EventoProtocoloEntity> findByProtocoloIdOrderByDiasAposInicioAsc(Long protocoloId);

    List<EventoProtocoloEntity> findByProtocoloIdAndTipo(Long protocoloId, TipoEventoProtocolo tipo);
}
