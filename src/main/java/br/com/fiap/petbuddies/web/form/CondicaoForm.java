package br.com.fiap.petbuddies.web.form;

import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/** O que a veterinária preenche numa condição; código, clínica e autor o controller resolve. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoForm {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres.")
    private String rotulo;

    @NotNull(message = "Tipo é obrigatório.")
    private TipoDado tipoDado = TipoDado.BOOLEANO;

    @NotNull(message = "Fonte é obrigatória.")
    private TipoFonteValor fonteValor = TipoFonteValor.RELATO;

    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
    private String unidade;

    private boolean critica;

    // O DDL não exige; a regra numérica sem unidade sairia sem "°C" na prévia e na receita.
    @AssertTrue(message = "Condição numérica precisa de unidade.")
    public boolean isUnidadeQuandoNumerica() {
        return tipoDado != TipoDado.NUMERICO || (unidade != null && !unidade.isBlank());
    }

    public static CondicaoForm from(CondicaoClinicaResponse condicao) {
        CondicaoForm form = new CondicaoForm();
        form.rotulo = condicao.getRotulo();
        form.tipoDado = condicao.getTipoDado();
        form.fonteValor = condicao.getFonteValor();
        form.unidade = condicao.getUnidade();
        form.critica = Boolean.TRUE.equals(condicao.getCritica());
        return form;
    }
}
