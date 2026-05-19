package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProtocoloRepository extends JpaRepository<ProtocoloEntity, Long> {

    List<ProtocoloEntity> findByAtivoTrue();

    List<ProtocoloEntity> findByCategoriaAndAtivoTrue(CategoriaProtocolo categoria);

    List<ProtocoloEntity> findByCategoriaAndEspecieAndAtivoTrue(CategoriaProtocolo categoria, Especie especie);

    @Query("SELECT p FROM ProtocoloEntity p WHERE p.categoria = :categoria AND p.especie = :especie AND p.porte = :porte AND p.sexo = :sexo AND p.castrado = :castrado AND p.ativo = true")
    Optional<ProtocoloEntity> findProtocoloExato(
            @Param("categoria") CategoriaProtocolo categoria, @Param("especie") Especie especie,
            @Param("porte") Porte porte, @Param("sexo") Sexo sexo, @Param("castrado") Boolean castrado);
}
