package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventoPlanoRepository extends JpaRepository<EventoPlanoEntity, Long> {

    List<EventoPlanoEntity> findByPlanoIdOrderByDataAlvoAsc(Long planoId);

    List<EventoPlanoEntity> findByPlanoIdAndStatus(Long planoId, StatusEventoPlano status);

    List<EventoPlanoEntity> findByDataAlvoBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusEventoPlano status);

    Page<EventoPlanoEntity> findByPlano_PetNetApiAnimalId(Long petNetApiAnimalId, Pageable pageable);

    List<EventoPlanoEntity> findByPlano_PetNetApiAnimalIdAndTipoAndStatusAndDataAlvoBefore(
            Long petNetApiAnimalId, TipoEventoProtocolo tipo, StatusEventoPlano status, LocalDate data);
}
