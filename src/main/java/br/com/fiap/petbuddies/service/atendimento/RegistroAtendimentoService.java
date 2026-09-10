package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.dto.atendimento.RegistroAtendimentoRequest;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistroAtendimentoService {

    private final RegistroAtendimentoRepository repository;
    private final AnimalRepository animalRepository;
    private final ConsultaRepository consultaRepository;

    public RegistroAtendimentoService(
            RegistroAtendimentoRepository repository,
            AnimalRepository animalRepository,
            ConsultaRepository consultaRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.consultaRepository = consultaRepository;
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
