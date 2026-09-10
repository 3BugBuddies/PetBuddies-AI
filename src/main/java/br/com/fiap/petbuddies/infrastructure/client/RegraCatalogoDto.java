package br.com.fiap.petbuddies.infrastructure.client;

import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;

/**
 * Molde de item, como o .NET devolve dentro de {@link ProtocoloCatalogoDto}.
 * O campo chama-se {@code dataBase}, e nao {@code ancora}: e o nome da
 * propriedade no JSON de {@code RegraProtocoloDto} do .NET.
 */
public record RegraCatalogoDto(
        Long id,
        Long protocoloId,
        TipoCuidado tipo,
        String nome,
        Integer offset,
        UnidadeTempo unidadeOffset,
        TipoDataBase dataBase,
        Integer intervalo,
        UnidadeTempo unidadeIntervalo,
        Integer repeticoes,
        String descricao) {
}
