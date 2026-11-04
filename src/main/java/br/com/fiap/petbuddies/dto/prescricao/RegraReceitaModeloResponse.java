package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.entity.RegraReceitaModeloEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegraReceitaModeloResponse {

    private Long id;
    private Long condicaoClinicaId;
    private String condicaoRotulo;
    private TipoDado condicaoTipoDado;
    private String condicaoUnidade;
    private OperadorRegra operador;
    private BigDecimal limite;
    private TipoAcaoRegra acaoDose;
    private Integer ordem;

    public static RegraReceitaModeloResponse from(RegraReceitaModeloEntity entity) {
        RegraReceitaModeloResponse dto = new RegraReceitaModeloResponse();
        dto.id = entity.getId();
        dto.condicaoClinicaId = entity.getCondicaoClinica().getId();
        dto.condicaoRotulo = entity.getCondicaoClinica().getRotulo();
        dto.condicaoTipoDado = entity.getCondicaoClinica().getTipoDado();
        dto.condicaoUnidade = entity.getCondicaoClinica().getUnidade();
        dto.operador = entity.getOperador();
        dto.limite = entity.getLimite();
        dto.acaoDose = entity.getAcaoDose();
        dto.ordem = entity.getOrdem();
        return dto;
    }
}
