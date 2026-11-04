package br.com.fiap.petbuddies.dto.prescricao;

import lombok.*;

import java.util.List;
import java.util.Map;

/** O rascunho que a extração devolve ao veterinário — nunca persistido. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RascunhoPrescricaoResponse {

    private String narrativaOriginal;
    private boolean extracaoDisponivel;
    private String motivoDegradacao;
    /** Um item por medicamento extraído, na ordem da narrativa. */
    private List<RascunhoItem> itens;
    /** LEGADO: primeiro item de {@code itens}. Nulo quando a extração degrada. */
    private PrescricaoRequest prescricao;
    /** LEGADO: primeiro item de {@code itens}. */
    private Map<String, Double> confiancaPorCampo;
    /** LEGADO: primeiro item de {@code itens}. */
    private List<RegraPropostaResponse> regrasPropostas;
    private List<String> condicoesDescartadas;

    public record RascunhoItem(
            PrescricaoRequest prescricao,
            Map<String, Double> confiancaPorCampo,
            List<RegraPropostaResponse> regrasPropostas) {}

    public static RascunhoPrescricaoResponse degradado(String narrativa, String motivo) {
        RascunhoPrescricaoResponse dto = new RascunhoPrescricaoResponse();
        dto.narrativaOriginal = narrativa;
        dto.extracaoDisponivel = false;
        dto.motivoDegradacao = motivo;
        dto.itens = List.of();
        dto.prescricao = null;
        dto.confiancaPorCampo = Map.of();
        dto.regrasPropostas = List.of();
        dto.condicoesDescartadas = List.of();
        return dto;
    }

    public static RascunhoPrescricaoResponse de(String narrativa, List<RascunhoItem> itens, List<String> condicoesDescartadas) {
        RascunhoItem primeiro = itens.get(0);
        RascunhoPrescricaoResponse dto = new RascunhoPrescricaoResponse();
        dto.narrativaOriginal = narrativa;
        dto.extracaoDisponivel = true;
        dto.motivoDegradacao = null;
        dto.itens = itens;
        dto.prescricao = primeiro.prescricao();
        dto.confiancaPorCampo = primeiro.confiancaPorCampo();
        dto.regrasPropostas = primeiro.regrasPropostas();
        dto.condicoesDescartadas = condicoesDescartadas;
        return dto;
    }
}
