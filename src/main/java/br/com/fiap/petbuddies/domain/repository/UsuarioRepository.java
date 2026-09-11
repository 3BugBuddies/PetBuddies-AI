package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Precisa ficar em domain.repository — fora dos pacotes declarados nos dois contextos de persistencia, o bean nao e criado.
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    // Filtro por ativo esta aqui, de proposito: usuario inativo devolve o mesmo vazio de login inexistente.
    Optional<UsuarioEntity> findByLoginAndAtivoTrue(String login);
}
