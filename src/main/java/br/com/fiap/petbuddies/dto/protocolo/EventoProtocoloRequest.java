package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.enums.TipoAncora;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Molde de um item de cuidado dentro de um protocolo")
public class EventoProtocoloRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "VACINACAO")
    @NotNull
    private TipoEventoProtocolo tipo;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Vacina múltipla V10")
    @NotBlank
    private String nome;

    @Schema(description = "Quanto somar à âncora para chegar na data do item", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer offset;

    @Schema(description = "Unidade do deslocamento", example = "MESES",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UnidadeTempo unidadeOffset;

    @Schema(description = "Data-base do deslocamento", example = "NASCIMENTO",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private TipoAncora ancora;

    @Schema(description = "Intervalo entre repetições. Nulo significa ocorrência única", example = "1")
    @Min(1)
    private Integer intervalo;

    @Schema(description = "Unidade do intervalo. Obrigatória quando há intervalo", example = "MESES")
    private UnidadeTempo unidadeIntervalo;

    @Schema(description = "Quantas vezes o item ocorre, contando a primeira", example = "6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    private Integer repeticoes;

    @Schema(example = "Reforço anual obrigatório")
    private String descricao;

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public UnidadeTempo getUnidadeOffset() { return unidadeOffset; }
    public void setUnidadeOffset(UnidadeTempo unidadeOffset) { this.unidadeOffset = unidadeOffset; }

    public TipoAncora getAncora() { return ancora; }
    public void setAncora(TipoAncora ancora) { this.ancora = ancora; }

    public Integer getIntervalo() { return intervalo; }
    public void setIntervalo(Integer intervalo) { this.intervalo = intervalo; }

    public UnidadeTempo getUnidadeIntervalo() { return unidadeIntervalo; }
    public void setUnidadeIntervalo(UnidadeTempo unidadeIntervalo) { this.unidadeIntervalo = unidadeIntervalo; }

    public Integer getRepeticoes() { return repeticoes; }
    public void setRepeticoes(Integer repeticoes) { this.repeticoes = repeticoes; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
