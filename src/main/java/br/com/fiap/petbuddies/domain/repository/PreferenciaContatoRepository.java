package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.PreferenciaContatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaContatoRepository extends JpaRepository<PreferenciaContatoEntity, Long> {

    Optional<PreferenciaContatoEntity> findByPetNetApiResponsavelId(Long petNetApiResponsavelId);
}
