package br.com.fiap.petbuddies.dto.cuidado;

import br.com.fiap.petbuddies.domain.enums.cuidado.MotivoSugestao;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

// Nao e gravada em nenhuma tabela — nada muda ate o veterinario aceitar.
@Schema(description = "Sugestão de próximo cuidado, derivada do histórico do animal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SugestaoCuidadoDto {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)")
    private Long animalId;

    @Schema(description = "Tipo do cuidado sugerido")
    private TipoCuidado tipo;

    @Schema(description = "Nome descritivo do cuidado")
    private String nome;

    @Schema(description = "Data em que o cuidado venceu ou deveria ter acontecido. "
        + "Nulo quando o cuidado nunca foi feito e não há data de referência")
    private LocalDate dataVencimento;

    @Schema(description = "Por que este cuidado está sendo sugerido")
    private MotivoSugestao motivo;

    @Schema(description = "ID do item já materializado no plano, quando motivo = REFORCO_VENCIDO. "
        + "Nulo para RECORRENCIA_DEVIDA e NUNCA_REALIZADO — não existe item, só a regra do catálogo")
    private Long itemId;

    public static SugestaoCuidadoDto reforcoVencido(Long animalId, Long itemId, TipoCuidado tipo,
                                                      String nome, LocalDate dataAlvo) {
        SugestaoCuidadoDto dto = new SugestaoCuidadoDto();
        dto.animalId = animalId;
        dto.itemId = itemId;
        dto.tipo = tipo;
        dto.nome = nome;
        dto.dataVencimento = dataAlvo;
        dto.motivo = MotivoSugestao.REFORCO_VENCIDO;
        return dto;
    }

    public static SugestaoCuidadoDto porHistoricoDeTipo(Long animalId, TipoCuidado tipo, String nome,
                                                          LocalDate dataVencimento, MotivoSugestao motivo) {
        SugestaoCuidadoDto dto = new SugestaoCuidadoDto();
        dto.animalId = animalId;
        dto.tipo = tipo;
        dto.nome = nome;
        dto.dataVencimento = dataVencimento;
        dto.motivo = motivo;
        return dto;
    }
}
