package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoRequest;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.prescricao.PrescricaoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Sem atualizar/remover: a prescrição é imutável depois de assinada (ADR s3-09). */
@Service
public class PrescricaoService {

    private final PrescricaoRepository repository;
    private final AnimalRepository animalRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;

    public PrescricaoService(
            PrescricaoRepository repository,
            AnimalRepository animalRepository,
            VeterinarioRepository veterinarioRepository,
            RegistroAtendimentoRepository registroAtendimentoRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<PrescricaoEntity> listar(Long animalId, Long registroAtendimentoId) {
        if (animalId != null) {
            return repository.findByAnimalIdOrderByDataInicioDesc(animalId);
        }
        if (registroAtendimentoId != null) {
            return repository.findByRegistroAtendimentoId(registroAtendimentoId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public PrescricaoEntity buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new PrescricaoNaoEncontradaException(id));
    }

    @Transactional
    public PrescricaoEntity criar(PrescricaoRequest request) {
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));
        VeterinarioEntity veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(request.getVeterinarioId()));
        RegistroAtendimentoEntity registroAtendimento = registroAtendimentoRepository.findById(request.getRegistroAtendimentoId())
                .orElseThrow(() -> new RegistroAtendimentoNaoEncontradoException(request.getRegistroAtendimentoId()));

        PrescricaoEntity entity = new PrescricaoEntity();
        entity.setMedicamento(request.getMedicamento());
        entity.setDoseMin(request.getDoseMin());
        entity.setDoseMax(request.getDoseMax());
        entity.setUnidade(request.getUnidade());
        entity.setFrequenciaDia(request.getFrequenciaDia());
        entity.setDuracaoDias(request.getDuracaoDias());
        entity.setDataInicio(request.getDataInicio());
        entity.setOrientacao(request.getOrientacao());
        entity.setMaterialOrigemId(request.getMaterialOrigemId());
        entity.setVersaoOrigem(request.getVersaoOrigem());
        entity.setAnimal(animal);
        entity.setVeterinario(veterinario);
        entity.setRegistroAtendimento(registroAtendimento);
        return repository.save(entity);
    }
}
