package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Precisa ficar em domain.repository — fora dos pacotes declarados nos dois contextos de persistencia, o bean nao e criado.
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    // Filtro por ativo esta aqui, de proposito: usuario inativo devolve o mesmo vazio de login inexistente.
    Optional<UsuarioEntity> findByLoginAndAtivoTrue(String login);

    boolean existsByLoginIgnoreCase(String login);

    List<UsuarioEntity> findByVeterinarioId(Long veterinarioId);

    @Query("SELECT u.responsavelId FROM UsuarioEntity u WHERE u.ativo = true AND u.responsavelId IN :responsavelIds")
    List<Long> findResponsaveisComAcessoAtivo(@Param("responsavelIds") Collection<Long> responsavelIds);

    boolean existsByResponsavelId(Long responsavelId);

    boolean existsByVeterinarioId(Long veterinarioId);
}
