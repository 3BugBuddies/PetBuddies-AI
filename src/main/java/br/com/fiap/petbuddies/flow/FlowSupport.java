package br.com.fiap.petbuddies.flow;

import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.flow.dto.AnimalResumo;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class FlowSupport {

    private FlowSupport() {}

    static Map<String, Object> dados(Object... valores) {
        Map<String, Object> dados = new LinkedHashMap<>();
        for (int i = 0; i + 1 < valores.length; i += 2) {
            if (valores[i] != null && valores[i + 1] != null) {
                dados.put(String.valueOf(valores[i]), valores[i + 1]);
            }
        }
        return dados;
    }

    static String nomeOuAnimal(String nome) {
        return nome == null || nome.isBlank() ? "o animal" : nome;
    }

    static Optional<AnimalDto> resolverAnimal(List<AnimalDto> animais, String mensagem) {
        if (animais.size() == 1) return Optional.of(animais.get(0));
        String texto = RespostaParserService.normalizar(mensagem);
        return animais.stream()
                .filter(a -> a.getNome() != null && texto.contains(RespostaParserService.normalizar(a.getNome())))
                .findFirst();
    }

    static Optional<AnimalResumo> resolverAnimalResumo(List<AnimalResumo> animais, String mensagem,
                                                        RespostaParserService parser) {
        Optional<Integer> ordem = parser.interpretarOrdem(mensagem);
        if (ordem.isPresent() && ordem.get() >= 1 && ordem.get() <= animais.size()) {
            return Optional.of(animais.get(ordem.get() - 1));
        }
        String texto = RespostaParserService.normalizar(mensagem);
        return animais.stream()
                .filter(a -> a.getNome() != null && texto.contains(RespostaParserService.normalizar(a.getNome())))
                .findFirst();
    }

    static String formatarAnimais(List<AnimalDto> animais) {
        return animais.stream()
                .map(a -> a.getNome() + " (" + a.getEspecie() + ")")
                .collect(Collectors.joining(", "));
    }

    static String formatarAnimaisResumo(List<AnimalResumo> animais) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < animais.size(); i++) {
            if (i > 0) sb.append(", ");
            AnimalResumo a = animais.get(i);
            sb.append(i + 1).append(". ").append(a.getNome()).append(" (").append(a.getEspecie()).append(")");
        }
        return sb.toString();
    }

    static List<AnimalResumo> toAnimaisResumo(List<AnimalDto> animais) {
        return animais.stream().map(a -> {
            AnimalResumo resumo = new AnimalResumo();
            resumo.setAnimalId(a.getId());
            resumo.setNome(a.getNome());
            resumo.setEspecie(a.getEspecie());
            return resumo;
        }).collect(Collectors.toList());
    }
}
