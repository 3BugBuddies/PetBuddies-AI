package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.ProcedimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.dto.atendimento.RegistroAtendimentoRequest;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaDeOutroAnimalException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoComVinculosException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistroAtendimentoService {

    private final RegistroAtendimentoRepository repository;
    private final AnimalRepository animalRepository;
    private final ConsultaRepository consultaRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final PrescricaoRepository prescricaoRepository;

    public RegistroAtendimentoService(
            RegistroAtendimentoRepository repository,
            AnimalRepository animalRepository,
            ConsultaRepository consultaRepository,
            ProcedimentoRepository procedimentoRepository,
            PrescricaoRepository prescricaoRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.consultaRepository = consultaRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional(readOnly = true)
    public List<RegistroAtendimentoEntity> listar(Long animalId, Long consultaId) {
        if (animalId != null) {
            return repository.findByAnimalIdOrderByDataAtendimentoDesc(animalId);
        }
        if (consultaId != null) {
            return repository.findByConsultaId(consultaId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public RegistroAtendimentoEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public RegistroAtendimentoEntity criar(RegistroAtendimentoRequest request) {
        RegistroAtendimentoEntity entity = new RegistroAtendimentoEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public RegistroAtendimentoEntity atualizar(Long id, RegistroAtendimentoRequest request) {
        RegistroAtendimentoEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        if (procedimentoRepository.existsByRegistroAtendimentoId(id)) {
            throw new RegistroAtendimentoComVinculosException(id, "procedimentos");
        }
        if (prescricaoRepository.existsByRegistroAtendimentoId(id)) {
            throw new RegistroAtendimentoComVinculosException(id, "prescrições");
        }
        repository.deleteById(id);
    }

    private RegistroAtendimentoEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RegistroAtendimentoNaoEncontradoException(id));
    }

    private void aplicar(RegistroAtendimentoRequest request, RegistroAtendimentoEntity entity) {
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));
        ConsultaEntity consulta = consultaRepository.findById(request.getConsultaId())
                .orElseThrow(() -> new ConsultaNaoEncontradaException(request.getConsultaId()));
        if (!consulta.getAnimal().getId().equals(animal.getId())) {
            throw new ConsultaDeOutroAnimalException(consulta.getId(), animal.getId());
        }
        entity.setDataAtendimento(request.getDataAtendimento());
        entity.setAnamnese(request.getAnamnese());
        entity.setDiagnostico(request.getDiagnostico());
        entity.setTratamento(request.getTratamento());
        entity.setObservacao(request.getObservacao());
        entity.setProximoRetorno(request.getProximoRetorno());
        entity.setProximaVacina(request.getProximaVacina());
        entity.setAnimal(animal);
        entity.setConsulta(consulta);
    }
}
