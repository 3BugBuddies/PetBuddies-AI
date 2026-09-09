package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.dto.ClinicaRequest;
import br.com.fiap.petbuddies.exception.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.CnpjDuplicadoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicaService {

    private final ClinicaRepository repository;

    public ClinicaService(ClinicaRepository repository) {
        this.repository = repository;
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
        repository.deleteById(id);
    }

    private ClinicaEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ClinicaNaoEncontradaException(id));
    }

    private void aplicar(ClinicaRequest request, ClinicaEntity entity) {
        entity.setNome(request.getNome());
        entity.setCnpj(request.getCnpj());
        entity.setTelefone(request.getTelefone());
        entity.setEmail(request.getEmail());
    }
}
