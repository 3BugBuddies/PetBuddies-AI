package br.com.fiap.petbuddies.domain.repository;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProtocoloRepository extends JpaRepository<ProtocoloEntity, Long> {

    List<ProtocoloEntity> findByAtivoTrue();

    List<ProtocoloEntity> findByCategoriaAndAtivoTrue(CategoriaProtocolo categoria);

    List<ProtocoloEntity> findByCategoriaAndEspecieAndAtivoTrue(CategoriaProtocolo categoria, Especie especie);

    Optional<ProtocoloEntity> findByCategoriaAndEspecieAndPorteAndSexoAndCastradoAndAtivoTrue(
            CategoriaProtocolo categoria, Especie especie, Porte porte, Sexo sexo, Boolean castrado);
}
