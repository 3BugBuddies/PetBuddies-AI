package br.com.fiap.petbuddies.dto.checkin;

import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoObservadaResponse {

    private Long id;
    private Long condicaoClinicaId;
    private String codigoCongelado;
    private Boolean valorBooleano;
    private BigDecimal valorNumerico;
    private BigDecimal confianca;
    private LocalDateTime createdAt;

    public static CondicaoObservadaResponse from(CondicaoObservadaEntity entity) {
        CondicaoObservadaResponse dto = new CondicaoObservadaResponse();
        dto.id = entity.getId();
        dto.condicaoClinicaId = entity.getCondicaoClinica() == null ? null : entity.getCondicaoClinica().getId();
        dto.codigoCongelado = entity.getCodigoCongelado();
        dto.valorBooleano = entity.getValorBooleano();
        dto.valorNumerico = entity.getValorNumerico();
        dto.confianca = entity.getConfianca();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }
}
