package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponsavelRepository extends JpaRepository<ResponsavelEntity, Long> {

    List<ResponsavelEntity> findByNomeContainingIgnoreCase(String nome);
}
