package br.com.fiap.petbuddies.dto.checkin;

import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import lombok.*;

import java.math.BigDecimal;

// Ausencia na lista e o nulo explicito — o tutor nao falou nela. Presente com valorBooleano=false e "mencionada e negada"; os dois nunca colapsam no mesmo valor.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoExtraidaResponse {

    private Long condicaoClinicaId;
    private String codigo;
    private String rotulo;
    private TipoDado tipoDado;
    private String unidade;
    private Boolean valorBooleano;
    private BigDecimal valorNumerico;
    private Double confianca;
    private boolean critica;
    /** Palavras exatas da narrativa que embasam a extração — não persistido, só para a tela de confirmação. */
    private String trecho;
    /** true se o tutor falou diretamente; false se a IA inferiu. Não persistido. */
    private boolean literal;

    public static CondicaoExtraidaResponse of(
            Long condicaoClinicaId, String codigo, String rotulo, TipoDado tipoDado, String unidade,
            Boolean valorBooleano, BigDecimal valorNumerico, Double confianca, boolean critica,
            String trecho, boolean literal) {
        CondicaoExtraidaResponse dto = new CondicaoExtraidaResponse();
        dto.condicaoClinicaId = condicaoClinicaId;
        dto.codigo = codigo;
        dto.rotulo = rotulo;
        dto.tipoDado = tipoDado;
        dto.unidade = unidade;
        dto.valorBooleano = valorBooleano;
        dto.valorNumerico = valorNumerico;
        dto.confianca = confianca;
        dto.critica = critica;
        dto.trecho = trecho;
        dto.literal = literal;
        return dto;
    }
}
