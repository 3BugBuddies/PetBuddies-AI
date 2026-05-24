package br.com.fiap.petbuddies.service.bot;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RespostaParserService {

    private static final Pattern NUMERO = Pattern.compile("\\b([1-5])\\b");
    private static final Pattern HORA = Pattern.compile("\\b(\\d{1,2})h\\b");
    private static final Pattern DATA_BR = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b");
    private static final Pattern DATA_ISO = Pattern.compile("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b");
    private static final Pattern ANO = Pattern.compile("\\b(20\\d{2}|19\\d{2})\\b");
    private static final Pattern IDADE_ANOS = Pattern.compile("\\b(\\d{1,2})\\s+anos?\\b");
    private static final Pattern IDADE_MESES = Pattern.compile("\\b(\\d{1,2})\\s+mes(?:es)?\\b");
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public enum RespostaBinaria {
        AFIRMATIVO, NEGATIVO, INDEFINIDO
    }

    public enum TempoRelatado {
        HORAS, HOJE, DIAS, SEMANAS, INDEFINIDO
    }

    public RespostaBinaria interpretarBinaria(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return RespostaBinaria.INDEFINIDO;

        if (contemAlgum(texto, "nao", "não", "nem", "sem", "nunca", "de jeito nenhum")) {
            return RespostaBinaria.NEGATIVO;
        }
        if (texto.matches("^(sim|s)$")
                || contemAlgum(texto, "tem", "esta", "está", "com dor", "dor forte",
                "apatia", "dificuldade", "respirar", "sangue", "vomitando", "vomitos",
                "convuls", "piora", "come", "bebe", "normal", "normalmente")) {
            return RespostaBinaria.AFIRMATIVO;
        }
        return RespostaBinaria.INDEFINIDO;
    }

    public TempoRelatado interpretarTempo(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return TempoRelatado.INDEFINIDO;

        if (contemAlgum(texto, "semana", "semanas", "mes", "meses")) {
            return TempoRelatado.SEMANAS;
        }
        if (contemAlgum(texto, "ontem", "dias", "dia", "alguns dias", "varios dias")) {
            return TempoRelatado.DIAS;
        }
        if (contemAlgum(texto, "hoje", "de manha", "de tarde", "agora")) {
            return TempoRelatado.HOJE;
        }
        if (contemAlgum(texto, "hora", "horas", "minuto", "minutos", "pouco tempo")) {
            return TempoRelatado.HORAS;
        }
        return TempoRelatado.INDEFINIDO;
    }

    public Optional<Integer> interpretarOrdem(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "primeira", "primeiro", "opcao um", "opção um")) return Optional.of(1);
        if (contemAlgum(texto, "segunda", "segundo", "opcao dois", "opção dois")) return Optional.of(2);
        if (contemAlgum(texto, "terceira", "terceiro", "opcao tres", "opção três")) return Optional.of(3);
        if (contemAlgum(texto, "quarta", "quarto", "opcao quatro", "opção quatro")) return Optional.of(4);
        if (contemAlgum(texto, "quinta", "quinto", "opcao cinco", "opção cinco")) return Optional.of(5);

        Matcher matcher = NUMERO.matcher(texto);
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }

    public Optional<Integer> interpretarHora(String mensagem) {
        String texto = normalizar(mensagem);
        Matcher matcher = HORA.matcher(texto);
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }

    public boolean isConfirmacao(String mensagem) {
        String texto = normalizar(mensagem);
        return texto.matches("^(sim|s|confirmo|confirmado|pode|pode sim|ok|certo|isso|fechado|quero|vamos).*");
    }

    public boolean isNegacao(String mensagem) {
        String texto = normalizar(mensagem);
        return texto.matches("^(nao|não|n|agora nao|melhor nao).*");
    }

    public Optional<String> interpretarTurno(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "manha", "manhã", "cedo", "pela manha", "de manha")) return Optional.of("MANHA");
        if (contemAlgum(texto, "tarde", "a tarde", "à tarde", "de tarde")) return Optional.of("TARDE");
        if (contemAlgum(texto, "noite", "a noite", "à noite", "de noite")) return Optional.of("NOITE");
        return Optional.empty();
    }

    public Optional<String> interpretarEspecie(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "cachorro", "cachorra", "cao", "cão", "dog", "canino")) return Optional.of("CACHORRO");
        if (contemAlgum(texto, "gato", "gata", "felino")) return Optional.of("GATO");
        if (contemAlgum(texto, "passaro", "pássaro", "ave", "calopsita", "canario", "canário")) return Optional.of("PASSARO");
        if (contemAlgum(texto, "coelho", "coelha")) return Optional.of("COELHO");
        if (contemAlgum(texto, "hamster")) return Optional.of("HAMSTER");
        if (contemAlgum(texto, "outro", "outra especie", "outra espécie")) return Optional.of("OUTRO");
        return Optional.empty();
    }

    public Optional<String> interpretarPorte(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "mini", "toy")) return Optional.of("MINI");
        if (contemAlgum(texto, "pequeno", "pequena", "pinscher", "chihuahua", "yorkshire", "maltês", "maltes", "spitz", "shih tzu")) return Optional.of("PEQUENO");
        if (contemAlgum(texto, "medio", "médio", "media", "média", "beagle", "cocker", "bulldog")) return Optional.of("MEDIO");
        if (contemAlgum(texto, "grande", "labrador", "golden", "husky", "border collie", "pastor alemao", "pastor alemão", "boxer")) return Optional.of("GRANDE");
        if (contemAlgum(texto, "gigante", "sao bernardo", "são bernardo", "dogue", "great dane", "fila", "rottweiler")) return Optional.of("GIGANTE");
        return Optional.empty();
    }

    public Optional<String> interpretarSexo(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "macho", "masculino")) return Optional.of("MACHO");
        if (contemAlgum(texto, "femea", "fêmea", "feminino")) return Optional.of("FEMEA");
        if (texto.matches(".*\\b(ele|meu)\\b.*")) return Optional.of("MACHO");
        if (texto.matches(".*\\b(ela|minha)\\b.*")) return Optional.of("FEMEA");
        return Optional.empty();
    }

    public Optional<Boolean> interpretarCastrado(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        if (contemAlgum(texto, "nao castrado", "não castrado", "nao castrada", "não castrada", "inteiro", "inteira")) {
            return Optional.of(false);
        }
        if (contemAlgum(texto, "castrado", "castrada", "castrei", "castrou")) {
            return Optional.of(true);
        }
        if (texto.matches("^(sim|s|confirmo|confirmado|ok|certo|isso)$")) return Optional.of(true);
        if (texto.matches("^(nao|não|n)$")) return Optional.of(false);
        return Optional.empty();
    }

    public Optional<String> interpretarDataNascimento(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.isBlank()) return Optional.empty();

        Matcher iso = DATA_ISO.matcher(texto);
        if (iso.find()) {
            return validarData(iso.group());
        }

        Matcher dataBr = DATA_BR.matcher(texto);
        if (dataBr.find()) {
            String valor = dataBr.group(3) + "-"
                    + String.format("%02d", Integer.parseInt(dataBr.group(2))) + "-"
                    + String.format("%02d", Integer.parseInt(dataBr.group(1)));
            return validarData(valor);
        }

        LocalDate hoje = LocalDate.now(ZONE_ID);
        Matcher idadeAnos = IDADE_ANOS.matcher(texto);
        if (idadeAnos.find()) {
            int anos = Integer.parseInt(idadeAnos.group(1));
            return Optional.of(LocalDate.of(hoje.minusYears(anos).getYear(), 1, 1).format(ISO));
        }

        Matcher idadeMeses = IDADE_MESES.matcher(texto);
        if (idadeMeses.find()) {
            int meses = Integer.parseInt(idadeMeses.group(1));
            return Optional.of(hoje.minusMonths(meses).withDayOfMonth(1).format(ISO));
        }

        Matcher ano = ANO.matcher(texto);
        if (ano.find()) {
            int valor = Integer.parseInt(ano.group(1));
            if (valor <= hoje.getYear()) {
                return Optional.of(LocalDate.of(valor, 1, 1).format(ISO));
            }
        }
        return Optional.empty();
    }

    public static String normalizar(String valor) {
        if (valor == null) return "";
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    private static boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(normalizar(termo))) {
                return true;
            }
        }
        return false;
    }

    private static Optional<String> validarData(String valor) {
        try {
            LocalDate data = LocalDate.parse(valor, ISO);
            if (data.isAfter(LocalDate.now(ZONE_ID))) {
                return Optional.empty();
            }
            return Optional.of(data.format(ISO));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
