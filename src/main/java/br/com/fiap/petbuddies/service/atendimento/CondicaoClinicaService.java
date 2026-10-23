package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoObservadaRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraReceitaModeloRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaRequest;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.CodigoCondicaoDuplicadoException;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaComVinculosException;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CondicaoClinicaService {

    private final CondicaoClinicaRepository repository;
    private final ClinicaRepository clinicaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final RegraPrescricaoRepository regraPrescricaoRepository;
    private final RegraReceitaModeloRepository regraReceitaModeloRepository;
    private final CondicaoObservadaRepository condicaoObservadaRepository;

    public CondicaoClinicaService(
            CondicaoClinicaRepository repository,
            ClinicaRepository clinicaRepository,
            VeterinarioRepository veterinarioRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            RegraReceitaModeloRepository regraReceitaModeloRepository,
            CondicaoObservadaRepository condicaoObservadaRepository) {
        this.repository = repository;
        this.clinicaRepository = clinicaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.regraPrescricaoRepository = regraPrescricaoRepository;
        this.regraReceitaModeloRepository = regraReceitaModeloRepository;
        this.condicaoObservadaRepository = condicaoObservadaRepository;
    }

    @Transactional(readOnly = true)
    public List<CondicaoClinicaEntity> listar(Long clinicaId) {
        if (clinicaId != null) {
            return repository.findByClinicaId(clinicaId);
        }
        return repository.findAll();
    }

    /** As condições da clínica por rótulo, cada uma com quantas regras (assinadas e de receita-modelo) a usam. */
    @Transactional(readOnly = true)
    public List<CondicaoClinicaResponse> listarComUso(Long clinicaId) {
        Map<Long, Long> usos = Stream.concat(
                        regraPrescricaoRepository.contarPorCondicaoDaClinica(clinicaId).stream(),
                        regraReceitaModeloRepository.contarPorCondicaoDaClinica(clinicaId).stream())
                .collect(Collectors.toMap(linha -> (Long) linha[0], linha -> (Long) linha[1], Long::sum));
        return repository.findByClinicaId(clinicaId).stream()
                .sorted(Comparator.comparing(CondicaoClinicaEntity::getRotulo, String.CASE_INSENSITIVE_ORDER))
                .map(c -> CondicaoClinicaResponse.from(c).comUso(usos.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    /** Sem nenhuma ativa marcada como sinal de alarme, o check-in não escala nada para a clínica. */
    @Transactional(readOnly = true)
    public boolean temSinalDeAlarme(Long clinicaId) {
        return repository.existsByClinicaIdAndCriticaTrueAndAtivoTrue(clinicaId);
    }

    /** Só as ativas, na ordem do rótulo: é o que a prescrição oferece. */
    @Transactional(readOnly = true)
    public List<CondicaoClinicaResponse> listarAtivas(Long clinicaId) {
        return repository.findByClinicaId(clinicaId).stream()
                .filter(CondicaoClinicaEntity::isAtivo)
                .sorted(Comparator.comparing(CondicaoClinicaEntity::getRotulo, String.CASE_INSENSITIVE_ORDER))
                .map(CondicaoClinicaResponse::from)
                .toList();
    }

    /** Condição de outra clínica é tratada como inexistente. */
    @Transactional(readOnly = true)
    public CondicaoClinicaResponse buscarDaClinica(Long id, Long clinicaId) {
        CondicaoClinicaEntity entity = encontrarOuFalhar(id);
        if (!clinicaId.equals(entity.getClinica().getId())) {
            throw new CondicaoClinicaNaoEncontradaException(id);
        }
        return CondicaoClinicaResponse.from(entity);
    }

    /** Regra assinada, regra de receita-modelo ou relato já guardado: o tipo do dado não pode mais mudar. */
    @Transactional(readOnly = true)
    public boolean emUso(Long id) {
        return regraPrescricaoRepository.existsByCondicaoClinicaId(id)
                || regraReceitaModeloRepository.existsByCondicaoClinicaId(id)
                || condicaoObservadaRepository.existsByCondicaoClinicaId(id);
    }

    /** Desativar não apaga: a condição some das opções, mas regras e relatos antigos continuam inteiros. */
    @Transactional
    public CondicaoClinicaEntity alterarSituacao(Long id, boolean ativo) {
        CondicaoClinicaEntity entity = encontrarOuFalhar(id);
        entity.setAtivo(ativo);
        return repository.save(entity);
    }

    /** "Dificuldade respiratória" vira DIFICULDADE_RESPIRATORIA: sem acento, maiúsculo, só letras, números e "_". */
    public static String gerarCodigo(String rotulo) {
        String semAcento = Normalizer.normalize(rotulo == null ? "" : rotulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String codigo = semAcento.toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
        return codigo.length() > 60 ? codigo.substring(0, 60) : codigo;
    }

    @Transactional(readOnly = true)
    public CondicaoClinicaEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public CondicaoClinicaEntity buscarPorCodigo(Long clinicaId, String codigo) {
        return repository.findByClinicaIdAndCodigo(clinicaId, codigo)
                .orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(clinicaId, codigo));
    }

    @Transactional
    public CondicaoClinicaEntity criar(CondicaoClinicaRequest request) {
        if (repository.existsByClinicaIdAndCodigo(request.getClinicaId(), request.getCodigo())) {
            throw new CodigoCondicaoDuplicadoException(request.getClinicaId(), request.getCodigo());
        }
        CondicaoClinicaEntity entity = new CondicaoClinicaEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public CondicaoClinicaEntity atualizar(Long id, CondicaoClinicaRequest request) {
        CondicaoClinicaEntity entity = encontrarOuFalhar(id);
        if (repository.existsByClinicaIdAndCodigoAndIdNot(request.getClinicaId(), request.getCodigo(), id)) {
            throw new CodigoCondicaoDuplicadoException(request.getClinicaId(), request.getCodigo());
        }
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        if (regraPrescricaoRepository.existsByCondicaoClinicaId(id)) {
            throw new CondicaoClinicaComVinculosException(id, "regras de prescrição");
        }
        if (regraReceitaModeloRepository.existsByCondicaoClinicaId(id)) {
            throw new CondicaoClinicaComVinculosException(id, "regras de receita-modelo");
        }
        if (condicaoObservadaRepository.existsByCondicaoClinicaId(id)) {
            throw new CondicaoClinicaComVinculosException(id, "condições observadas");
        }
        repository.deleteById(id);
    }

    private CondicaoClinicaEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(id));
    }

    private void aplicar(CondicaoClinicaRequest request, CondicaoClinicaEntity entity) {
        ClinicaEntity clinica = clinicaRepository.findById(request.getClinicaId())
                .orElseThrow(() -> new ClinicaNaoEncontradaException(request.getClinicaId()));
        VeterinarioEntity autor = veterinarioRepository.findById(request.getVeterinarioAutorId())
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(request.getVeterinarioAutorId()));
        entity.setCodigo(request.getCodigo());
        entity.setRotulo(request.getRotulo());
        entity.setTipoDado(request.getTipoDado());
        entity.setFonteValor(request.getFonteValor() == null ? TipoFonteValor.RELATO : request.getFonteValor());
        entity.setUnidade(request.getUnidade());
        entity.setCritica(Boolean.TRUE.equals(request.getCritica()));
        entity.setAtivo(request.getAtivo() == null || request.getAtivo());
        entity.setClinica(clinica);
        entity.setAutor(autor);
    }
}
