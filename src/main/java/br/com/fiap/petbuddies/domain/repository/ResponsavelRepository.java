package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponsavelRepository extends JpaRepository<ResponsavelEntity, Long> {

    /**
     * Busca por nome parcial. E a unica busca do recurso: nao ha campo unico em
     * T_PB_RESPONSAVEL, entao nao existe "buscar pelo documento".
     */
    List<ResponsavelEntity> findByNomeContainingIgnoreCase(String nome);
}
