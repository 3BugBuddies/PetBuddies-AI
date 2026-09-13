package br.com.fiap.petbuddies.domain.embeddable;

import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Desfecho {

    @Column(name = "ID_CHECKIN")
    private Long checkinId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DESFECHO", length = 30)
    private TipoDesfecho tipo;

    @Column(name = "NR_DOSE_APLICADA", precision = 8, scale = 3)
    private BigDecimal doseAplicada;

    @Column(name = "ID_REGRA_APLICADA")
    private Long regraAplicadaId;
}
