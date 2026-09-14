package br.com.fiap.petbuddies.domain.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.NumericBooleanConverter;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ValorObservado {

    // Nulo quando a condicao e NUMERICO. JPA nao invoca o converter para valor
    // nulo, entao o Boolean (wrapper) fica nulo em vez de virar 0 (CK_COBS_UM_VALOR).
    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "BL_VALOR_BOOLEANO")
    private Boolean booleano;

    // Nulo quando a condicao e BOOLEANO (CK_COBS_UM_VALOR).
    @Column(name = "NR_VALOR_NUMERICO", precision = 10, scale = 3)
    private BigDecimal numerico;
}
