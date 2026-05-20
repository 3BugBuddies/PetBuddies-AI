package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.NotificacaoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Long> {

    boolean existsByEventoPlanoId(Long eventoPlanoId);

    List<NotificacaoEntity> findByStatusEnvioAndCreatedAtAfter(StatusNotificacao statusEnvio, LocalDateTime desde);
}
