package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.cuidado.UnidadeTempo;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.cuidado.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.modelo.ModeloPlanoResponse;
import br.com.fiap.petbuddies.dto.modelo.ModeloPlanoResponse.CuidadoModelo;
import br.com.fiap.petbuddies.exception.cuidado.ModeloPlanoNaoEncontradoException;
import br.com.fiap.petbuddies.infrastructure.client.ProtocoloCatalogoDto;
import br.com.fiap.petbuddies.infrastructure.client.ProtocoloClient;
import br.com.fiap.petbuddies.infrastructure.client.RegraCatalogoDto;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Leitura dos modelos de plano que vivem no .NET. Sem {@code @Transactional} de propósito:
 * há chamada HTTP no meio e ela não pode segurar conexão do pool.
 * Optional vazio em todo método significa catálogo fora do ar.
 */
@Service
public class ModeloPlanoService {

    private final ProtocoloClient protocoloClient;
    private final MotorPlanoService motorPlanoService;
    private final AnimalRepository animalRepository;

    public ModeloPlanoService(
            ProtocoloClient protocoloClient, MotorPlanoService motorPlanoService, AnimalRepository animalRepository) {
        this.protocoloClient = protocoloClient;
        this.motorPlanoService = motorPlanoService;
        this.animalRepository = animalRepository;
    }

    public Optional<Map<Especie, List<ModeloPlanoResponse>>> listarPorEspecie() {
        return protocoloClient.listarAtivos().map(protocolos -> protocolos.stream()
                .map(p -> resumir(p, null, null, null, null))
                .sorted(Comparator.comparing(ModeloPlanoResponse::nome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(ModeloPlanoResponse::especie, TreeMap::new, Collectors.toList())));
    }

    public Optional<ModeloPlanoResponse> detalhar(Long id, Long animalId) {
        return protocoloClient.listarAtivos().map(protocolos -> {
            ProtocoloCatalogoDto protocolo = protocolos.stream()
                    .filter(p -> p.id().equals(id)).findFirst()
                    .orElseThrow(() -> new ModeloPlanoNaoEncontradoException(id));
            List<AnimalResponse> animais = animalRepository.findAllComResponsavel().stream()
                    .filter(a -> a.getEspecie() == protocolo.especie())
                    .sorted(Comparator.comparing(AnimalEntity::getNome, String.CASE_INSENSITIVE_ORDER))
                    .map(AnimalResponse::from)
                    .toList();
            AnimalResponse escolhido = animais.stream().filter(a -> a.getId().equals(animalId)).findFirst().orElse(null);
            return previa(protocolo, animais, escolhido);
        });
    }

    private ModeloPlanoResponse previa(ProtocoloCatalogoDto protocolo, List<AnimalResponse> animais, AnimalResponse escolhido) {
        if (protocolo.categoria() == CategoriaProtocolo.POS_CIRURGICO) {
            return resumir(protocolo, animais, null, null,
                    "Este modelo conta da data da cirurgia: não há prévia por animal.");
        }
        if (escolhido == null) {
            return resumir(protocolo, animais, null, null, null);
        }
        boolean dependeDoHistorico = protocolo.regras().stream().anyMatch(r -> r.dataBase() == TipoDataBase.ULTIMA_REALIZACAO);
        String aviso = dependeDoHistorico
                ? "Cuidados contados da última vez só entram depois que o primeiro for registrado."
                : null;
        return resumir(protocolo, animais, escolhido,
                motorPlanoService.prever(protocolo, escolhido.getDataNascimento()), aviso);
    }

    private ModeloPlanoResponse resumir(
            ProtocoloCatalogoDto p, List<AnimalResponse> animais, AnimalResponse escolhido,
            List<ItemPlanoCuidadoDto> previa, String aviso) {
        List<CuidadoModelo> cuidados = p.regras().stream().map(this::cuidado).toList();
        return new ModeloPlanoResponse(p.id(), p.nome(), p.categoria(), p.especie(), p.ativo(), p.descricao(),
                cuidados, animais, escolhido, previa, aviso);
    }

    private CuidadoModelo cuidado(RegraCatalogoDto r) {
        boolean repete = r.intervalo() != null && r.intervalo() > 0 && r.unidadeIntervalo() != null
                && r.repeticoes() != null && r.repeticoes() > 1;
        return new CuidadoModelo(r.nome(), r.tipo(), comeca(r),
                repete ? "a cada " + quantidade(r.intervalo(), r.unidadeIntervalo()) : "—",
                repete ? r.repeticoes() : 1);
    }

    private String comeca(RegraCatalogoDto r) {
        int deslocamento = r.offset() == null ? 0 : r.offset();
        UnidadeTempo unidade = r.unidadeOffset() == null ? UnidadeTempo.DIAS : r.unidadeOffset();
        TipoDataBase base = r.dataBase();
        if (base == TipoDataBase.NASCIMENTO) {
            return deslocamento == 0 ? "no nascimento" : "aos " + quantidade(deslocamento, unidade);
        }
        String noMomento = switch (base) {
            case null -> "no cadastro";
            case DATA_CIRURGIA -> "no dia da cirurgia";
            case ULTIMA_REALIZACAO -> "na última vez";
            case NASCIMENTO -> "no nascimento";
        };
        String depois = switch (base) {
            case null -> "o cadastro";
            case DATA_CIRURGIA -> "a cirurgia";
            case ULTIMA_REALIZACAO -> "a última vez";
            case NASCIMENTO -> "o nascimento";
        };
        return deslocamento == 0 ? noMomento : quantidade(deslocamento, unidade) + " após " + depois;
    }

    private String quantidade(int valor, UnidadeTempo unidade) {
        String nome = switch (unidade) {
            case DIAS -> valor == 1 ? "dia" : "dias";
            case SEMANAS -> valor == 1 ? "semana" : "semanas";
            case MESES -> valor == 1 ? "mês" : "meses";
        };
        return valor + " " + nome;
    }
}
