package br.com.fiap.petbuddies.domain.embeddable;

import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoCongelada {

    @Column(name = "DS_ROTULO_CONGELADO", nullable = false, length = 255)
    private String rotulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DADO_CONGELADO", nullable = false, length = 20)
    private TipoDado tipoDado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_FONTE_VALOR_CONGELADA", nullable = false, length = 20)
    private TipoFonteValor fonteValor;
}
