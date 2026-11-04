package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResponsavelRepository extends JpaRepository<ResponsavelEntity, Long> {

    List<ResponsavelEntity> findByNomeContainingIgnoreCase(String nome);

    List<ResponsavelEntity> findAllByOrderByNomeAsc();

    // :texto chega em minúsculas e já entre %; telefone só entra na busca quando o texto tem dígitos.
    @Query("SELECT r FROM ResponsavelEntity r WHERE LOWER(r.nome) LIKE :texto OR LOWER(r.contato.email) LIKE :texto ORDER BY r.nome")
    List<ResponsavelEntity> buscarPorTexto(@Param("texto") String texto);

    @Query("SELECT r FROM ResponsavelEntity r WHERE LOWER(r.nome) LIKE :texto OR LOWER(r.contato.email) LIKE :texto "
            + "OR r.contato.telefone LIKE :digitos ORDER BY r.nome")
    List<ResponsavelEntity> buscarPorTextoOuTelefone(@Param("texto") String texto, @Param("digitos") String digitos);

    boolean existsByContatoEmailIgnoreCase(String email);

    boolean existsByContatoTelefone(String telefone);
}
