package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.TriagemSessaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TriagemSessaoRepository extends JpaRepository<TriagemSessaoEntity, Long> {

    Optional<TriagemSessaoEntity> findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(String telefone);

    List<TriagemSessaoEntity> findByPetNetApiAnimalIdOrderByIniciadaEmDesc(Long petNetApiAnimalId);
}
