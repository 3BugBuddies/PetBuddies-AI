package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicaRepository extends JpaRepository<ClinicaEntity, Long> {

    Optional<ClinicaEntity> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    /**
     * A unicidade vista de dentro da atualizacao: o CNPJ pode continuar sendo o
     * da propria linha, e so colide se pertencer a outra.
     */
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
