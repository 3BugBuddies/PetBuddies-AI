package br.com.fiap.petbuddies.infrastructure.client;

import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;

import java.time.LocalDateTime;
import java.util.List;

/** O protocolo e suas regras, como o .NET devolve em {@code GET /api/protocolo}. */
public record ProtocoloCatalogoDto(
        Long id,
        String nome,
        CategoriaProtocolo categoria,
        Especie especie,
        boolean ativo,
        String descricao,
        LocalDateTime createdAt,
        List<RegraCatalogoDto> regras) {
}
