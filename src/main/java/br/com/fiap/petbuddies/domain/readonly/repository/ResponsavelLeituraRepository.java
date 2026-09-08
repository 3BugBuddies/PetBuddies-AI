package br.com.fiap.petbuddies.domain.readonly.repository;

import br.com.fiap.petbuddies.domain.readonly.ResponsavelLeitura;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Leitura do tutor no registro clinico. Somente consulta, pelo mesmo motivo da
 * {@link AnimalLeituraRepository}.
 */
public interface ResponsavelLeituraRepository extends Repository<ResponsavelLeitura, Long> {

    Optional<ResponsavelLeitura> findById(Long id);
}
