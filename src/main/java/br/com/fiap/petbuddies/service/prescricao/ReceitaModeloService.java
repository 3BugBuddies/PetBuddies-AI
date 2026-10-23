package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.embeddable.FaixaDose;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import br.com.fiap.petbuddies.domain.entity.RegraReceitaModeloEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.ReceitaModeloRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloRequest;
import br.com.fiap.petbuddies.dto.prescricao.RegraReceitaModeloRequest;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.exception.identidade.UsuarioNaoVeterinarioException;
import br.com.fiap.petbuddies.exception.prescricao.ReceitaModeloIncoerenteException;
import br.com.fiap.petbuddies.exception.prescricao.ReceitaModeloNaoEncontradaException;
import br.com.fiap.petbuddies.exception.prescricao.TituloReceitaModeloDuplicadoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Clínica e autor vêm sempre do usuário autenticado. A prescrição copia o molde; nada aqui toca prescrição assinada. */
@Service
public class ReceitaModeloService {

    private final ReceitaModeloRepository repository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ReceitaModeloService(
            ReceitaModeloRepository repository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            UsuarioRepository usuarioRepository,
            VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ReceitaModeloEntity> listar(Long usuarioId) {
        Long clinicaId = veterinarioDoUsuario(usuarioId).getClinica().getId();
        return repository.findByClinicaIdAndAtivoTrueOrderByTituloAsc(clinicaId);
    }

    @Transactional(readOnly = true)
    public ReceitaModeloEntity buscarPorId(Long id, Long usuarioId) {
        return doUsuario(id, veterinarioDoUsuario(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<CondicaoClinicaEntity> listarCondicoesAtivas(Long usuarioId) {
        Long clinicaId = veterinarioDoUsuario(usuarioId).getClinica().getId();
        return condicaoClinicaRepository.findByClinicaId(clinicaId).stream()
                .filter(CondicaoClinicaEntity::isAtivo)
                .sorted((a, b) -> a.getRotulo().compareToIgnoreCase(b.getRotulo()))
                .toList();
    }

    @Transactional
    public ReceitaModeloEntity criar(ReceitaModeloRequest request, Long usuarioId) {
        VeterinarioEntity autor = veterinarioDoUsuario(usuarioId);
        Long clinicaId = autor.getClinica().getId();
        if (repository.existsByClinicaIdAndTitulo(clinicaId, request.getTitulo())) {
            throw new TituloReceitaModeloDuplicadoException(request.getTitulo());
        }
        ReceitaModeloEntity entity = new ReceitaModeloEntity();
        entity.setClinica(autor.getClinica());
        entity.setAutor(autor);
        aplicar(request, entity, clinicaId);
        return repository.save(entity);
    }

    /** Substitui conteúdo e regras e sobe a versão. O autor original é mantido. */
    @Transactional
    public ReceitaModeloEntity atualizar(Long id, ReceitaModeloRequest request, Long usuarioId) {
        VeterinarioEntity vet = veterinarioDoUsuario(usuarioId);
        Long clinicaId = vet.getClinica().getId();
        ReceitaModeloEntity entity = doUsuario(id, vet);
        if (repository.existsByClinicaIdAndTituloAndIdNot(clinicaId, request.getTitulo(), id)) {
            throw new TituloReceitaModeloDuplicadoException(request.getTitulo());
        }
        aplicar(request, entity, clinicaId);
        entity.setVersao(entity.getVersao() + 1);
        return repository.save(entity);
    }

    @Transactional
    public void desativar(Long id, Long usuarioId) {
        ReceitaModeloEntity entity = doUsuario(id, veterinarioDoUsuario(usuarioId));
        entity.setAtivo(false);
        repository.save(entity);
    }

    private void aplicar(ReceitaModeloRequest request, ReceitaModeloEntity entity, Long clinicaId) {
        // Valida antes de mexer na entidade gerenciada: recusa não deixa estado pela metade.
        List<RegraReceitaModeloEntity> regras = montarRegras(request.getRegras(), clinicaId);
        entity.setTitulo(request.getTitulo());
        entity.setMedicamento(request.getMedicamento());
        entity.setFaixaDose(new FaixaDose(request.getDoseMin(), request.getDoseMax(), request.getUnidade()));
        entity.setFrequenciaDia(request.getFrequenciaDia());
        entity.setDuracaoDias(request.getDuracaoDias());
        entity.setOrientacao(request.getOrientacao());
        entity.substituirRegras(regras);
    }

    private List<RegraReceitaModeloEntity> montarRegras(List<RegraReceitaModeloRequest> requests, Long clinicaId) {
        List<RegraReceitaModeloEntity> regras = new ArrayList<>();
        if (requests == null) {
            return regras;
        }
        int ordem = 1;
        for (RegraReceitaModeloRequest r : requests) {
            CondicaoClinicaEntity condicao = condicaoClinicaRepository.findById(r.getCondicaoClinicaId())
                    .orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(r.getCondicaoClinicaId()));
            validarRegra(condicao, r, clinicaId);

            RegraReceitaModeloEntity regra = new RegraReceitaModeloEntity();
            regra.setCondicaoClinica(condicao);
            regra.setOperador(r.getOperador());
            regra.setLimite(r.getLimite());
            regra.setAcaoDose(r.getAcaoDose());
            regra.setOrdem(ordem++);
            regras.add(regra);
        }
        return regras;
    }

    private void validarRegra(CondicaoClinicaEntity condicao, RegraReceitaModeloRequest r, Long clinicaId) {
        if (!condicao.getClinica().getId().equals(clinicaId)) {
            throw new ReceitaModeloIncoerenteException(
                    "A condição \"" + condicao.getRotulo() + "\" não pertence à sua clínica.");
        }
        if (!condicao.isAtivo()) {
            throw new ReceitaModeloIncoerenteException("A condição \"" + condicao.getRotulo() + "\" está desativada.");
        }
        boolean numerica = condicao.getTipoDado() == TipoDado.NUMERICO;
        boolean comOperadorELimite = r.getOperador() != null && r.getLimite() != null;
        boolean semOperadorNemLimite = r.getOperador() == null && r.getLimite() == null;
        if (numerica && !comOperadorELimite) {
            throw new ReceitaModeloIncoerenteException(
                    "A condição \"" + condicao.getRotulo() + "\" é numérica e exige operador e limite.");
        }
        if (!numerica && !semOperadorNemLimite) {
            throw new ReceitaModeloIncoerenteException(
                    "A condição \"" + condicao.getRotulo() + "\" é de sim/não e não aceita operador nem limite.");
        }
    }

    // Receita de outra clínica ou já desativada some como 404, sem confirmar que existe.
    private ReceitaModeloEntity doUsuario(Long id, VeterinarioEntity vet) {
        ReceitaModeloEntity entity = repository.findComRegrasById(id)
                .orElseThrow(() -> new ReceitaModeloNaoEncontradaException(id));
        if (!entity.getClinica().getId().equals(vet.getClinica().getId()) || !entity.isAtivo()) {
            throw new ReceitaModeloNaoEncontradaException(id);
        }
        return entity;
    }

    private VeterinarioEntity veterinarioDoUsuario(Long usuarioId) {
        Long veterinarioId = usuarioRepository.findById(usuarioId)
                .orElseThrow(CredenciaisInvalidasException::new)
                .getVeterinarioId();
        if (veterinarioId == null) {
            throw new UsuarioNaoVeterinarioException();
        }
        return veterinarioRepository.findComClinicaById(veterinarioId)
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(veterinarioId));
    }
}
