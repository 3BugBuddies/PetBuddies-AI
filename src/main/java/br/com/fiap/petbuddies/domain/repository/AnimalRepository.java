package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {

    List<AnimalEntity> findByResponsavelId(Long responsavelId);

    List<AnimalEntity> findByNomeContainingIgnoreCase(String nome);
}
