package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.enums.identidade.PerfilUsuario;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CheckinRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.ProcedimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.dto.cadastro.AnimalRequest;
import br.com.fiap.petbuddies.exception.cadastro.AnimalComVinculosException;
import br.com.fiap.petbuddies.exception.cadastro.AnimalDeOutroTutorException;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelNaoEncontradoException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnimalService {

    /** Default de RC_RACA no DDL. */
    private static final String RACA_PADRAO = "SEM_RACA";

    private final AnimalRepository repository;
    private final ResponsavelRepository responsavelRepository;
    private final ConsultaRepository consultaRepository;
    private final PlanoCuidadoRepository planoCuidadoRepository;
    private final CheckinRepository checkinRepository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AnimalService(
            AnimalRepository repository,
            ResponsavelRepository responsavelRepository,
            ConsultaRepository consultaRepository,
            PlanoCuidadoRepository planoCuidadoRepository,
            CheckinRepository checkinRepository,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            ProcedimentoRepository procedimentoRepository,
            PrescricaoRepository prescricaoRepository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.responsavelRepository = responsavelRepository;
        this.consultaRepository = consultaRepository;
        this.planoCuidadoRepository = planoCuidadoRepository;
        this.checkinRepository = checkinRepository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimalEntity> listar(Long responsavelId, String nome) {
        if (responsavelId != null) {
            return repository.findByResponsavelId(responsavelId);
        }
        if (nome != null && !nome.isBlank()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AnimalEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public AnimalEntity criar(AnimalRequest request) {
        AnimalEntity entity = new AnimalEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public AnimalEntity atualizar(Long id, AnimalRequest request) {
        AnimalEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id, Long usuarioId) {
        AnimalEntity animal = encontrarOuFalhar(id);
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .filter(UsuarioEntity::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);
        if (usuario.getPerfil() == PerfilUsuario.TUTOR
                && !animal.getResponsavel().getId().equals(usuario.getResponsavelId())) {
            throw new AnimalDeOutroTutorException(id);
        }
        if (consultaRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "consultas");
        }
        if (planoCuidadoRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "planos de cuidado");
        }
        if (checkinRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "check-ins");
        }
        if (registroAtendimentoRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "registros de atendimento");
        }
        if (procedimentoRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "procedimentos");
        }
        if (prescricaoRepository.existsByAnimalId(id)) {
            throw new AnimalComVinculosException(id, "prescrições");
        }
        repository.deleteById(id);
    }

    private AnimalEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new AnimalNaoEncontradoException(id));
    }

    private void aplicar(AnimalRequest request, AnimalEntity entity) {
        ResponsavelEntity responsavel = responsavelRepository.findById(request.getResponsavelId())
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(request.getResponsavelId()));
        entity.setNome(request.getNome());
        entity.setEspecie(request.getEspecie());
        entity.setRaca(request.getRaca() == null || request.getRaca().isBlank()
                ? RACA_PADRAO
                : request.getRaca());
        entity.setPorte(request.getPorte());
        entity.setSexo(request.getSexo());
        entity.setDataNascimento(request.getDataNascimento());
        entity.setPeso(request.getPeso());
        entity.setCondicaoCronica(Boolean.TRUE.equals(request.getCondicaoCronica()));
        entity.setCastrado(Boolean.TRUE.equals(request.getCastrado()));
        entity.setFoto(request.getFoto());
        entity.setAlergias(request.getAlergias());
        entity.setObservacoes(request.getObservacoes());
        entity.setResponsavel(responsavel);
    }
}
