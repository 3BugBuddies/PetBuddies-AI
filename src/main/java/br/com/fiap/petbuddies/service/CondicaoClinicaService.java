package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.CondicaoClinicaRequest;
import br.com.fiap.petbuddies.exception.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.CodigoCondicaoDuplicadoException;
import br.com.fiap.petbuddies.exception.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CondicaoClinicaService {

    private final CondicaoClinicaRepository repository;
    private final ClinicaRepository clinicaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public CondicaoClinicaService(
            CondicaoClinicaRepository repository,
            ClinicaRepository clinicaRepository,
            VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.clinicaRepository = clinicaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CondicaoClinicaEntity> listar(Long clinicaId) {
        if (clinicaId != null) {
            return repository.findByClinicaId(clinicaId);
        }
        return repository.findAll();
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
