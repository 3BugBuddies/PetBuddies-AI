package br.com.fiap.petbuddies.apresentacao;

import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoProcedimento;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Porte;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.cuidado.UnidadeTempo;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Texto de tela dos enums, num lugar só: os enums ficam como o banco os conhece. Disponível nos templates como {@code @rotulos}. */
@Component("rotulos")
public class Rotulos {

    private static final String VAZIO = "—";

    public String de(Enum<?> valor) {
        return switch (valor) {
            case null -> VAZIO;
            case Especie e -> especie(e);
            case Porte p -> porte(p);
            case Sexo s -> s == Sexo.MACHO ? "Macho" : "Fêmea";
            case TipoConsulta t -> tipoConsulta(t);
            case StatusConsulta s -> statusConsulta(s);
            case StatusItem s -> statusItem(s, false);
            case TipoCuidado t -> tipoCuidado(t);
            case CategoriaPlano c -> categoria(c.name());
            case CategoriaProtocolo c -> categoria(c.name());
            case TipoProcedimento t -> tipoProcedimento(t);
            case TipoDado t -> t == TipoDado.BOOLEANO ? "Sim/não" : "Número";
            case TipoFonteValor f -> fonte(f);
            case OperadorRegra o -> operador(o);
            case TipoAcaoRegra a -> acao(a);
            case TipoDesfecho d -> desfecho(d);
            case UnidadeTempo u -> unidade(u);
            case TipoDataBase b -> dataBase(b);
            default -> valor.name();
        };
    }

    /** Item de prescrição fala em dose; item de protocolo fala em cuidado feito. */
    public String statusItem(StatusItem status, boolean daPrescricao) {
        return switch (status) {
            case null -> VAZIO;
            case PENDENTE -> "Pendente";
            case ATRASADO -> "Atrasado";
            case CANCELADO -> "Cancelado";
            case REALIZADO -> daPrescricao ? "Dose dada" : "Feito";
            case SUSPENSO -> daPrescricao ? "Sem dose" : "Suspenso";
            case ENCAMINHADO -> daPrescricao ? "Clínica acionada" : "Encaminhado";
        };
    }

    public String numero(BigDecimal valor) {
        return valor == null ? VAZIO : valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** "temperatura > 39,5 °C → acionar clínica" ou "comeu pouco → dose mínima". */
    public String regra(String condicao, OperadorRegra operador, BigDecimal limite, String unidade, TipoAcaoRegra acao) {
        StringBuilder texto = new StringBuilder(condicao == null ? VAZIO : condicao.toLowerCase());
        if (operador != null && limite != null) {
            texto.append(' ').append(operador(operador)).append(' ').append(numero(limite));
            if (unidade != null && !unidade.isBlank()) {
                texto.append(' ').append(unidade);
            }
        }
        return texto.append(" → ").append(acao(acao)).toString();
    }

    private String especie(Especie e) {
        return switch (e) {
            case CACHORRO -> "Cão";
            case GATO -> "Gato";
            case PASSARO -> "Pássaro";
            case COELHO -> "Coelho";
            case HAMSTER -> "Hamster";
            case OUTRO -> "Outro";
        };
    }

    private String porte(Porte p) {
        return switch (p) {
            case MINI -> "Mini";
            case PEQUENO -> "Pequeno";
            case MEDIO -> "Médio";
            case GRANDE -> "Grande";
            case GIGANTE -> "Gigante";
        };
    }

    private String tipoConsulta(TipoConsulta t) {
        return switch (t) {
            case ROTINA -> "Rotina";
            case VACINACAO -> "Vacinação";
            case EXAME -> "Exame";
            case RETORNO -> "Retorno";
            case EMERGENCIA -> "Emergência";
        };
    }

    private String statusConsulta(StatusConsulta s) {
        return switch (s) {
            case AGENDADA -> "Agendada";
            case CONFIRMADA -> "Confirmada";
            case REALIZADA -> "Realizada";
            case CANCELADA -> "Cancelada";
            case NAO_COMPARECEU -> "Não compareceu";
        };
    }

    private String tipoCuidado(TipoCuidado t) {
        return switch (t) {
            case VACINACAO -> "Vacinação";
            case VERMIFUGACAO -> "Vermifugação";
            case EXAME -> "Exame";
            case RETORNO -> "Retorno";
            case CIRURGIA -> "Cirurgia";
            case MEDICACAO -> "Medicação";
            case HIGIENE -> "Higiene";
        };
    }

    private String categoria(String nome) {
        return switch (nome) {
            case "PREVENTIVO" -> "Preventivo";
            case "POS_CIRURGICO" -> "Pós-cirúrgico";
            case "TRATAMENTO" -> "Tratamento";
            default -> nome;
        };
    }

    private String tipoProcedimento(TipoProcedimento t) {
        return switch (t) {
            case VACINACAO -> "Vacinação";
            case VERMIFUGACAO -> "Vermifugação";
            case EXAME_LABORATORIAL -> "Exame laboratorial";
            case EXAME_IMAGEM -> "Exame de imagem";
            case CIRURGIA -> "Cirurgia";
            case INTERNACAO -> "Internação";
            case OUTRO -> "Outro";
        };
    }

    private String fonte(TipoFonteValor f) {
        return switch (f) {
            case RELATO -> "Relato do responsável";
            case ANIMAL -> "Do animal";
            case HISTORICO -> "Do histórico";
        };
    }

    private String operador(OperadorRegra o) {
        return switch (o) {
            case MAIOR_QUE -> ">";
            case MAIOR_OU_IGUAL -> "≥";
            case MENOR_QUE -> "<";
            case MENOR_OU_IGUAL -> "≤";
            case IGUAL -> "=";
        };
    }

    private String acao(TipoAcaoRegra a) {
        return switch (a) {
            case null -> VAZIO;
            case DOSE_MIN -> "dose mínima";
            case DOSE_MAX -> "dose máxima";
            case DOSE_PADRAO -> "dose padrão";
            case ACIONAR_CLINICA -> "acionar clínica";
            case SUSPENDER -> "suspender";
        };
    }

    private String desfecho(TipoDesfecho d) {
        return switch (d) {
            case DOSE_CALCULADA -> "dose calculada";
            case ACIONAR_CLINICA -> "acionar clínica";
            case SEM_DOSE -> "sem dose";
        };
    }

    private String unidade(UnidadeTempo u) {
        return switch (u) {
            case DIAS -> "dias";
            case SEMANAS -> "semanas";
            case MESES -> "meses";
        };
    }

    private String dataBase(TipoDataBase b) {
        return switch (b) {
            case NASCIMENTO -> "do nascimento";
            case DATA_CIRURGIA -> "da cirurgia";
            case ULTIMA_REALIZACAO -> "da última vez";
        };
    }
}
