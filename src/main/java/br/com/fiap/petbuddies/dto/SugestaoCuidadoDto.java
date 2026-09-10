package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.MotivoSugestao;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Uma sugestão de próximo cuidado para o animal, a partir do histórico.
 * Não é gravada em nenhuma tabela — é a mesma escolha do ADR s3-24 §3 para
 * saldo e tier: nada muda até o veterinário aceitar.
 */
@Schema(description = "Sugestão de próximo cuidado, derivada do histórico do animal")
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

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public TipoCuidado getTipo() { return tipo; }
    public void setTipo(TipoCuidado tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public MotivoSugestao getMotivo() { return motivo; }
    public void setMotivo(MotivoSugestao motivo) { this.motivo = motivo; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}
