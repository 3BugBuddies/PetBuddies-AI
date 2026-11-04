package br.com.fiap.petbuddies.dto.modelo;

import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.cuidado.ItemPlanoCuidadoDto;
import java.util.List;

/**
 * Um modelo de plano do catálogo. Na lista só vão os sete primeiros campos; o detalhe preenche
 * {@code animais} e, quando há animal escolhido, {@code animalDaPrevia}, {@code previa} e/ou {@code avisoPrevia}.
 */
public record ModeloPlanoResponse(
        Long id,
        String nome,
        CategoriaProtocolo categoria,
        Especie especie,
        boolean ativo,
        String descricao,
        List<CuidadoModelo> cuidados,
        List<AnimalResponse> animais,
        AnimalResponse animalDaPrevia,
        List<ItemPlanoCuidadoDto> previa,
        String avisoPrevia) {

    public record CuidadoModelo(String nome, TipoCuidado tipo, String comeca, String repete, int vezes) {
    }
}
