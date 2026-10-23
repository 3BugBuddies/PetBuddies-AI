package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import lombok.*;

import java.math.BigDecimal;

/** Regra de dose proposta pela IA no rascunho; nunca persistida. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegraPropostaResponse {

    /** Nulo quando a condição ainda não existe no catálogo da clínica ({@code condicaoNova} vem preenchida). */
    private Long condicaoClinicaId;
    private String condicaoRotulo;
    private CondicaoNova condicaoNova;
    private OperadorRegra operador;
    private BigDecimal limite;
    private TipoAcaoRegra acaoDose;
    private Integer ordem;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CondicaoNova {
        private String rotulo;
        private TipoDado tipoDado;
    }
}
