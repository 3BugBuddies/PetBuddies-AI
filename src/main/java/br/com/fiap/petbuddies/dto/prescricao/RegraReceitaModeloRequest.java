package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.entity.RegraReceitaModeloEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

// Sem ordem: vale a posição na lista.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegraReceitaModeloRequest {

    @NotNull(message = "Condição clínica é obrigatória.")
    private Long condicaoClinicaId;

    /** Obrigatório quando a condição é NUMERICO; nulo quando é BOOLEANO. */
    private OperadorRegra operador;

    /** Obrigatório quando a condição é NUMERICO; nulo quando é BOOLEANO. */
    @Digits(integer = 7, fraction = 3, message = "Limite excede a precisão NUMBER(10,3).")
    private BigDecimal limite;

    @NotNull(message = "Ação sobre a dose é obrigatória.")
    private TipoAcaoRegra acaoDose;

    public static RegraReceitaModeloRequest from(RegraReceitaModeloEntity entity) {
        return new RegraReceitaModeloRequest(
                entity.getCondicaoClinica().getId(), entity.getOperador(), entity.getLimite(), entity.getAcaoDose());
    }
}
