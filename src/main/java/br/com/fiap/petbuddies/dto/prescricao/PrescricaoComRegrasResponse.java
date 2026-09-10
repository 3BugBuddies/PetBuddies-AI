package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/** Uma prescrição do fechamento, com as regras que foram assinadas junto. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrescricaoComRegrasResponse {

    private PrescricaoResponse prescricao;
    private List<RegraPrescricaoResponse> regras;

    public static PrescricaoComRegrasResponse from(PrescricaoEntity prescricao, List<RegraPrescricaoEntity> regras) {
        PrescricaoComRegrasResponse dto = new PrescricaoComRegrasResponse();
        dto.prescricao = PrescricaoResponse.from(prescricao);
        dto.regras = regras.stream().map(RegraPrescricaoResponse::from).collect(Collectors.toList());
        return dto;
    }
}
