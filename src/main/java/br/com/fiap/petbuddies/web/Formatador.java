package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Formatação de exibição para os templates: {@code ${@formatador.telefone(x)}}. O banco guarda só dígitos. */
@Component("formatador")
public class Formatador {

    private static final String VAZIO = "—";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_EXTENSO = DateTimeFormatter.ofPattern("EEEE, dd/MM", PT_BR);

    /** (11) 91234-5678 ou (11) 1234-5678; qualquer outro tamanho volta como veio. */
    public String telefone(String valor) {
        String d = digitos(valor);
        return switch (d.length()) {
            case 11 -> "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
            case 10 -> "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
            default -> valor == null ? "" : valor;
        };
    }

    /** 12.345.678/0001-99; fora de 14 dígitos volta como veio. */
    public String cnpj(String valor) {
        String d = digitos(valor);
        if (d.length() != 14) {
            return valor == null ? "" : valor;
        }
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/" + d.substring(8, 12) + "-" + d.substring(12);
    }

    /** 09h ou 09h30. */
    public String hora(LocalTime hora) {
        return hora.getMinute() == 0 ? String.format("%02dh", hora.getHour()) : String.format("%02dh%02d", hora.getHour(), hora.getMinute());
    }

    public String hora(LocalDateTime dataHora) {
        return hora(dataHora.toLocalTime());
    }

    /** seg 19 */
    public String diaCurto(LocalDate dia) {
        String semana = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "");
        return semana + " " + dia.getDayOfMonth();
    }

    /** Quinta, 22/10 */
    public String dataExtenso(LocalDate dia) {
        String texto = DATA_EXTENSO.format(dia);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** "Segunda-feira" com inicial maiúscula, sem o "-feira". */
    public String diaDaSemana(java.time.DayOfWeek dia) {
        String nome = dia.getDisplayName(TextStyle.FULL, PT_BR).replace("-feira", "");
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }

    public String saudacao(LocalDateTime agora) {
        int h = agora.getHour();
        return h < 12 ? "Bom dia" : (h < 18 ? "Boa tarde" : "Boa noite");
    }

    public String numero(BigDecimal valor) {
        return valor == null ? VAZIO : valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** "10–20 mg", ou "10 mg" quando mínimo e máximo coincidem. */
    public String faixa(BigDecimal min, BigDecimal max, String unidade) {
        String valor = min.compareTo(max) == 0 ? numero(min) : numero(min) + "–" + numero(max);
        return unidade == null ? valor : valor + " " + unidade;
    }

    /** "temperatura > 39,5 °C → acionar clínica" ou "comeu pouco → dose mínima". */
    public String regra(String condicao, OperadorRegra operador, BigDecimal limite, String unidade, TipoAcaoRegra acao) {
        StringBuilder texto = new StringBuilder(condicao == null ? VAZIO : condicao.toLowerCase());
        if (operador != null && limite != null) {
            texto.append(' ').append(operador.getRotulo()).append(' ').append(numero(limite));
            if (unidade != null && !unidade.isBlank()) {
                texto.append(' ').append(unidade);
            }
        }
        return texto.append(" → ").append(acao == null ? VAZIO : acao.getRotulo()).toString();
    }

    private String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
