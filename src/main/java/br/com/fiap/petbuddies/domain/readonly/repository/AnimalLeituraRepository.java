package br.com.fiap.petbuddies.domain.readonly.repository;

import br.com.fiap.petbuddies.domain.readonly.AnimalLeitura;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Leitura do pet no registro clinico.
 *
 * <p>Estende {@link Repository}, e nao {@code JpaRepository}, de proposito: a
 * interface expoe exatamente os metodos de consulta que existem, e nenhum
 * {@code save} ou {@code delete} — a projecao nunca escreve.</p>
 */
public interface AnimalLeituraRepository extends Repository<AnimalLeitura, Long> {

    Optional<AnimalLeitura> findById(Long id);

    /** Os pets de um tutor — o que o painel do {@code J4} lista. */
    List<AnimalLeitura> findByResponsavelId(Long responsavelId);
}
