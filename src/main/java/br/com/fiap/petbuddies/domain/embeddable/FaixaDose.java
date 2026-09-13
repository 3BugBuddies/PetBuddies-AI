package br.com.fiap.petbuddies.domain.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FaixaDose {

    @Column(name = "NR_DOSE_MIN", nullable = false, precision = 8, scale = 3)
    private BigDecimal doseMin;

    @Column(name = "NR_DOSE_MAX", nullable = false, precision = 8, scale = 3)
    private BigDecimal doseMax;

    @Column(name = "DS_UNIDADE", nullable = false, length = 20)
    private String unidade;
}
