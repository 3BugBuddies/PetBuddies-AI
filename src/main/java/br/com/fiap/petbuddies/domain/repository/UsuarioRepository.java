package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a credencial. Fica sob {@code domain.repository}, o pacote do contexto
 * de cuidado declarado no {@code CuidadoPersistenceConfig} — desde que existem
 * dois contextos de persistencia, repositorio fora dos dois pacotes declarados
 * simplesmente nao e criado, e o erro aparece como bean nao encontrado na
 * subida.
 */
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Unica busca de login que existe. O filtro por ativo esta aqui, e nao no
     * servico, de proposito: usuario inativo devolve o mesmo vazio de login
     * inexistente, e os dois caminhos terminam na mesma mensagem de 401.
     */
    Optional<UsuarioEntity> findByLoginAndAtivoTrue(String login);

    boolean existsByLoginIgnoreCase(String login);
}
