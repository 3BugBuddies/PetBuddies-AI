package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegraPrescricaoResponse {

    private Long id;
    private Long prescricaoId;
    private Long condicaoClinicaId;
    private String rotuloCongelado;
    private TipoDado tipoDadoCongelado;
    private TipoFonteValor fonteValorCongelada;
    private OperadorRegra operador;
    private BigDecimal limite;
    private TipoAcaoRegra acaoDose;
    private Integer ordem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RegraPrescricaoResponse from(RegraPrescricaoEntity entity) {
        RegraPrescricaoResponse dto = new RegraPrescricaoResponse();
        dto.id = entity.getId();
        dto.prescricaoId = entity.getPrescricao() == null ? null : entity.getPrescricao().getId();
        dto.condicaoClinicaId = entity.getCondicaoClinica() == null ? null : entity.getCondicaoClinica().getId();
        dto.rotuloCongelado = entity.getRotuloCongelado();
        dto.tipoDadoCongelado = entity.getTipoDadoCongelado();
        dto.fonteValorCongelada = entity.getFonteValorCongelada();
        dto.operador = entity.getOperador();
        dto.limite = entity.getLimite();
        dto.acaoDose = entity.getAcaoDose();
        dto.ordem = entity.getOrdem();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
