package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioRequest;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.CrmvDuplicadoException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeterinarioService {

    private final VeterinarioRepository repository;
    private final ClinicaRepository clinicaRepository;

    public VeterinarioService(VeterinarioRepository repository, ClinicaRepository clinicaRepository) {
        this.repository = repository;
        this.clinicaRepository = clinicaRepository;
    }

    @Transactional(readOnly = true)
    public List<VeterinarioEntity> listar(Long clinicaId) {
        if (clinicaId != null) {
            return repository.findByClinicaId(clinicaId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public VeterinarioEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public VeterinarioEntity buscarPorCrmv(String crmv) {
        return repository.findByCrmv(crmv)
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(crmv));
    }

    @Transactional
    public VeterinarioEntity criar(VeterinarioRequest request) {
        if (repository.existsByCrmv(request.getCrmv())) {
            throw new CrmvDuplicadoException(request.getCrmv());
        }
        VeterinarioEntity entity = new VeterinarioEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public VeterinarioEntity atualizar(Long id, VeterinarioRequest request) {
        VeterinarioEntity entity = encontrarOuFalhar(id);
        if (repository.existsByCrmvAndIdNot(request.getCrmv(), id)) {
            throw new CrmvDuplicadoException(request.getCrmv());
        }
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private VeterinarioEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new VeterinarioNaoEncontradoException(id));
    }

    private void aplicar(VeterinarioRequest request, VeterinarioEntity entity) {
        ClinicaEntity clinica = clinicaRepository.findById(request.getClinicaId())
                .orElseThrow(() -> new ClinicaNaoEncontradaException(request.getClinicaId()));
        entity.setNome(request.getNome());
        entity.setCrmv(request.getCrmv());
        entity.setEmail(request.getEmail());
        entity.setAtivo(request.getAtivo() == null || request.getAtivo());
        entity.setClinica(clinica);
    }
}
