package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.embeddable.Contato;
import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaRequest;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaComVinculosException;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.CnpjDuplicadoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicaService {

    private final ClinicaRepository repository;
    private final VeterinarioRepository veterinarioRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;

    public ClinicaService(
            ClinicaRepository repository,
            VeterinarioRepository veterinarioRepository,
            CondicaoClinicaRepository condicaoClinicaRepository) {
        this.repository = repository;
        this.veterinarioRepository = veterinarioRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicaEntity> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ClinicaEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public Optional<ClinicaEntity> buscarUnica() {
        return repository.findFirstByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public ClinicaEntity buscarPorCnpj(String cnpj) {
        return repository.findByCnpj(cnpj)
                .orElseThrow(() -> new ClinicaNaoEncontradaException(cnpj));
    }

    @Transactional
    public ClinicaEntity criar(ClinicaRequest request) {
        // Checagem previa para o duplicado virar 409 de dominio, e nao erro de driver em UK_CLINICA_CNPJ.
        if (repository.existsByCnpj(request.getCnpj())) {
            throw new CnpjDuplicadoException(request.getCnpj());
        }
        ClinicaEntity entity = new ClinicaEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public ClinicaEntity atualizar(Long id, ClinicaRequest request) {
        ClinicaEntity entity = encontrarOuFalhar(id);
        if (repository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new CnpjDuplicadoException(request.getCnpj());
        }
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        if (veterinarioRepository.existsByClinicaId(id)) {
            throw new ClinicaComVinculosException(id, "veterinários");
        }
        if (condicaoClinicaRepository.existsByClinicaId(id)) {
            throw new ClinicaComVinculosException(id, "condições clínicas");
        }
        repository.deleteById(id);
    }

    private ClinicaEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ClinicaNaoEncontradaException(id));
    }

    private void aplicar(ClinicaRequest request, ClinicaEntity entity) {
        entity.setNome(request.getNome());
        entity.setCnpj(request.getCnpj());
        entity.setContato(new Contato(request.getTelefone(), request.getEmail()));
    }
}
