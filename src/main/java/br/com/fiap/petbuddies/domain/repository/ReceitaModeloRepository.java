package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceitaModeloRepository extends JpaRepository<ReceitaModeloEntity, Long> {

    // Autor e regras (com a condição) vêm no mesmo select: o DTO é montado fora da transação.
    @EntityGraph(attributePaths = {"autor", "regras", "regras.condicaoClinica"})
    List<ReceitaModeloEntity> findByClinicaIdAndAtivoTrueOrderByTituloAsc(Long clinicaId);

    @EntityGraph(attributePaths = {"autor", "regras", "regras.condicaoClinica"})
    Optional<ReceitaModeloEntity> findComRegrasById(Long id);

    // UK_RECEITA_MODELO_TITULO
    boolean existsByClinicaIdAndTitulo(Long clinicaId, String titulo);

    boolean existsByClinicaIdAndTituloAndIdNot(Long clinicaId, String titulo, Long id);
}
