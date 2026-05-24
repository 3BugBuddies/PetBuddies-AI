package br.com.fiap.petbuddies.service.motor;

import br.com.fiap.petbuddies.domain.entity.TriagemSessaoEntity;
import br.com.fiap.petbuddies.domain.enums.ClassificacaoTriagem;
import br.com.fiap.petbuddies.flow.dto.TriagemScoreResultado;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;
import br.com.fiap.petbuddies.service.bot.RespostaParserService.RespostaBinaria;
import br.com.fiap.petbuddies.service.bot.RespostaParserService.TempoRelatado;
import org.springframework.stereotype.Service;

@Service
public class TriagemScoreService {

    private final RespostaParserService parserService;

    public TriagemScoreService(RespostaParserService parserService) {
        this.parserService = parserService;
    }

    public TriagemScoreResultado calcular(TriagemSessaoEntity sessao) {
        int score = pontuarSintoma(sessao.getSintomaPrincipal());

        if (parserService.interpretarBinaria(sessao.getRespostaP1()) == RespostaBinaria.NEGATIVO) {
            score += 8;
        }
        if (parserService.interpretarBinaria(sessao.getRespostaP2()) == RespostaBinaria.AFIRMATIVO) {
            score += 18;
        }

        TempoRelatado tempo = parserService.interpretarTempo(sessao.getRespostaP3());
        if (tempo == TempoRelatado.SEMANAS || tempo == TempoRelatado.DIAS) {
            score += 8;
        } else if (tempo == TempoRelatado.HOJE || tempo == TempoRelatado.HORAS) {
            score += 3;
        }

        if (parserService.interpretarBinaria(sessao.getRespostaP4()) == RespostaBinaria.AFIRMATIVO) {
            score += 20;
        }

        ClassificacaoTriagem classificacao = classificar(score);
        return new TriagemScoreResultado(score, classificacao, recomendacao(classificacao));
    }

    public boolean isEmergenciaSuspeita(String sintoma) {
        String texto = RespostaParserService.normalizar(sintoma);
        return contemAlgum(texto, "convuls", "sangue", "atropel", "intoxic",
                "dificuldade para respirar", "nao respira", "não respira", "engasgad");
    }

    private int pontuarSintoma(String sintoma) {
        String texto = RespostaParserService.normalizar(sintoma);
        if (isEmergenciaSuspeita(texto)) {
            return 30;
        }
        if (contemAlgum(texto, "vomit", "diarre", "febre", "apatia", "apatico", "apática")) {
            return 14;
        }
        if (contemAlgum(texto, "coceira", "olho", "lacrimej", "tosse", "toss")) {
            return 8;
        }
        return 3;
    }

    private ClassificacaoTriagem classificar(int score) {
        if (score <= 10) return ClassificacaoTriagem.PODE_ESPERAR;
        if (score <= 25) return ClassificacaoTriagem.PRIORITARIO;
        return ClassificacaoTriagem.EMERGENCIA;
    }

    private String recomendacao(ClassificacaoTriagem classificacao) {
        return switch (classificacao) {
            case PODE_ESPERAR -> "Pode observar em casa por enquanto. Mantenha água disponível e procure a clínica se persistir ou piorar.";
            case PRIORITARIO -> "Recomendamos consulta nas próximas 24 a 48 horas para avaliar com segurança.";
            case EMERGENCIA -> "Recomendamos levar à clínica ou atendimento veterinário imediatamente. Não medique por conta própria.";
        };
    }

    private static boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(RespostaParserService.normalizar(termo))) {
                return true;
            }
        }
        return false;
    }
}
