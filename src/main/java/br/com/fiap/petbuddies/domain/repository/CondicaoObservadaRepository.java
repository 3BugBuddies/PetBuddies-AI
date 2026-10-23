package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CondicaoObservadaRepository extends JpaRepository<CondicaoObservadaEntity, Long> {

    List<CondicaoObservadaEntity> findByCheckin_Id(Long checkinId);

    @Query("SELECT o FROM CondicaoObservadaEntity o JOIN FETCH o.condicaoClinica WHERE o.checkin.id IN :checkinIds")
    List<CondicaoObservadaEntity> findComCondicaoPorCheckins(@Param("checkinIds") Collection<Long> checkinIds);

    boolean existsByCondicaoClinicaId(Long condicaoClinicaId);
}
