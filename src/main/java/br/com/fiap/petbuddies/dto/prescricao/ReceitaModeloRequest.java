package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Sem clínica nem autor: vêm do usuário autenticado, nunca do corpo.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaModeloRequest {

    @NotBlank(message = "Título é obrigatório.")
    @Size(max = 120, message = "Título deve ter no máximo 120 caracteres.")
    private String titulo;

    @NotBlank(message = "Medicamento é obrigatório.")
    @Size(max = 150, message = "Medicamento deve ter no máximo 150 caracteres.")
    private String medicamento;

    @NotNull(message = "Dose mínima é obrigatória.")
    @Digits(integer = 5, fraction = 3, message = "Dose mínima excede a precisão NUMBER(8,3).")
    private BigDecimal doseMin;

    @NotNull(message = "Dose máxima é obrigatória.")
    @Digits(integer = 5, fraction = 3, message = "Dose máxima excede a precisão NUMBER(8,3).")
    private BigDecimal doseMax;

    @NotBlank(message = "Unidade da dose é obrigatória.")
    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
    private String unidade;

    @NotNull(message = "Frequência diária é obrigatória.")
    @Positive(message = "Frequência diária deve ser maior que zero.")
    @Max(value = 99, message = "Frequência diária excede a precisão NUMBER(2).")
    private Integer frequenciaDia;

    @NotNull(message = "Duração em dias é obrigatória.")
    @Positive(message = "Duração em dias deve ser maior que zero.")
    @Max(value = 9999, message = "Duração em dias excede a precisão NUMBER(4).")
    private Integer duracaoDias;

    private String orientacao;

    private List<@NotNull @Valid RegraReceitaModeloRequest> regras = new ArrayList<>();

    // CK_RECEITA_MODELO_FAIXA. public: o BeanWrapperImpl só lê getter público ao montar o erro.
    @AssertTrue(message = "Dose mínima não pode ser maior que a dose máxima.")
    public boolean isFaixaDoseValida() {
        return doseMin == null || doseMax == null || doseMin.compareTo(doseMax) <= 0;
    }

    public static ReceitaModeloRequest from(ReceitaModeloEntity entity) {
        ReceitaModeloRequest dto = new ReceitaModeloRequest();
        dto.titulo = entity.getTitulo();
        dto.medicamento = entity.getMedicamento();
        dto.doseMin = entity.getFaixaDose().getDoseMin();
        dto.doseMax = entity.getFaixaDose().getDoseMax();
        dto.unidade = entity.getFaixaDose().getUnidade();
        dto.frequenciaDia = entity.getFrequenciaDia();
        dto.duracaoDias = entity.getDuracaoDias();
        dto.orientacao = entity.getOrientacao();
        dto.regras = entity.getRegras().stream().map(RegraReceitaModeloRequest::from).collect(Collectors.toCollection(ArrayList::new));
        return dto;
    }
}
