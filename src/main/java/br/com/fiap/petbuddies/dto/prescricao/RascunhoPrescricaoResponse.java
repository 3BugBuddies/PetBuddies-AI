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
    private PrescricaoRequest prescricao;
    private Map<String, Double> confiancaPorCampo;
    private List<RegraPropostaResponse> regrasPropostas;
    private List<String> condicoesDescartadas;

    public static RascunhoPrescricaoResponse degradado(String narrativa, String motivo, PrescricaoRequest prescricaoVazia) {
        RascunhoPrescricaoResponse dto = new RascunhoPrescricaoResponse();
        dto.narrativaOriginal = narrativa;
        dto.extracaoDisponivel = false;
        dto.motivoDegradacao = motivo;
        dto.prescricao = prescricaoVazia;
        dto.confiancaPorCampo = Map.of();
        dto.regrasPropostas = List.of();
        dto.condicoesDescartadas = List.of();
        return dto;
    }

    public static RascunhoPrescricaoResponse de(
            String narrativa,
            PrescricaoRequest prescricao,
            Map<String, Double> confiancaPorCampo,
            List<RegraPropostaResponse> regrasPropostas,
            List<String> condicoesDescartadas) {
        RascunhoPrescricaoResponse dto = new RascunhoPrescricaoResponse();
        dto.narrativaOriginal = narrativa;
        dto.extracaoDisponivel = true;
        dto.motivoDegradacao = null;
        dto.prescricao = prescricao;
        dto.confiancaPorCampo = confiancaPorCampo;
        dto.regrasPropostas = regrasPropostas;
        dto.condicoesDescartadas = condicoesDescartadas;
        return dto;
    }
}
