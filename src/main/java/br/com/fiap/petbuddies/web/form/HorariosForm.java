package br.com.fiap.petbuddies.web.form;

import br.com.fiap.petbuddies.dto.atendimento.ModeloSemanal;
import br.com.fiap.petbuddies.dto.atendimento.ModeloSemanal.Faixa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/** O modelo semanal digitado na tela de horários. Vive só na requisição. */
@Getter
@Setter
@NoArgsConstructor
public class HorariosForm {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FaixaForm {
        private DayOfWeek dia;

        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime inicio;

        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime fim;

        public FaixaForm(DayOfWeek dia, LocalTime inicio, LocalTime fim) {
            this.dia = dia;
            this.inicio = inicio;
            this.fim = fim;
        }
    }

    private List<FaixaForm> faixas = new ArrayList<>();

    @NotNull(message = "Informe a duração da consulta.")
    @Min(value = 5, message = "A duração mínima é de 5 minutos.")
    @Max(value = 240, message = "A duração máxima é de 240 minutos.")
    private Integer duracao = 30;

    @NotNull(message = "Informe o número de semanas.")
    @Min(value = 1, message = "Gere ao menos 1 semana.")
    @Max(value = 8, message = "Gere no máximo 8 semanas.")
    private Integer semanas = 4;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> bloqueados = new ArrayList<>();

    /** Segunda a sexta das 9h às 12h e das 14h às 17h; sábado de manhã. */
    public static HorariosForm padrao() {
        HorariosForm form = new HorariosForm();
        for (DayOfWeek dia : DayOfWeek.values()) {
            if (dia == DayOfWeek.SUNDAY) {
                continue;
            }
            form.faixas.add(new FaixaForm(dia, LocalTime.of(9, 0), LocalTime.of(12, 0)));
            if (dia != DayOfWeek.SATURDAY) {
                form.faixas.add(new FaixaForm(dia, LocalTime.of(14, 0), LocalTime.of(17, 0)));
            }
        }
        return form;
    }

    /** Linha sem horário nenhum (removida ou em branco) não conta; meia linha segue e a validação do modelo recusa. */
    public ModeloSemanal toModelo() {
        List<Faixa> validas = faixas.stream()
                .filter(Objects::nonNull)
                .filter(f -> f.getDia() != null && (f.getInicio() != null || f.getFim() != null))
                .map(f -> new Faixa(f.getDia(), f.getInicio(), f.getFim()))
                .toList();
        return new ModeloSemanal(validas, duracao, semanas,
                bloqueados.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
    }
}
