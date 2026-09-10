package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.prescricao.RegraPrescricaoRequest;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.prescricao.PrescricaoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoIncoerenteException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Sem atualizar/remover: a regra é imutável como a prescrição que a carrega. */
@Service
public class RegraPrescricaoService {

    private final RegraPrescricaoRepository repository;
    private final PrescricaoRepository prescricaoRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;

    public RegraPrescricaoService(
            RegraPrescricaoRepository repository,
            PrescricaoRepository prescricaoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository) {
        this.repository = repository;
        this.prescricaoRepository = prescricaoRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
    }

    @Transactional(readOnly = true)
    public List<RegraPrescricaoEntity> listar(Long prescricaoId) {
        if (prescricaoId != null) {
            return repository.findByPrescricaoIdOrderByOrdemAsc(prescricaoId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public RegraPrescricaoEntity buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RegraPrescricaoNaoEncontradaException(id));
    }

    @Transactional
    public RegraPrescricaoEntity criar(RegraPrescricaoRequest request) {
        PrescricaoEntity prescricao = prescricaoRepository.findById(request.getPrescricaoId())
                .orElseThrow(() -> new PrescricaoNaoEncontradaException(request.getPrescricaoId()));
        CondicaoClinicaEntity condicao = condicaoClinicaRepository.findById(request.getCondicaoClinicaId())
                .orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(request.getCondicaoClinicaId()));

        validarCoerencia(condicao.getTipoDado(), request.getOperador(), request.getLimite());

        RegraPrescricaoEntity entity = new RegraPrescricaoEntity();
        entity.setPrescricao(prescricao);
        entity.setCondicaoClinica(condicao);
        // A cópia congelada (ADR s3-10): o que foi assinado, não o que o catálogo diz hoje.
        entity.setRotuloCongelado(condicao.getRotulo());
        entity.setTipoDadoCongelado(condicao.getTipoDado());
        entity.setFonteValorCongelada(condicao.getFonteValor());
        entity.setOperador(request.getOperador());
        entity.setLimite(request.getLimite());
        entity.setAcaoDose(request.getAcaoDose());
        entity.setOrdem(request.getOrdem());
        return repository.save(entity);
    }

    // CK_REGRA_COERENCIA: NUMERICO exige operador e limite; BOOLEANO não aceita nenhum dos dois.
    private void validarCoerencia(TipoDado tipoDado, Object operador, Object limite) {
        boolean numerico = tipoDado == TipoDado.NUMERICO;
        boolean temOperadorELimite = operador != null && limite != null;
        boolean semOperadorNemLimite = operador == null && limite == null;
        if (numerico && !temOperadorELimite) {
            throw new RegraPrescricaoIncoerenteException(
                    "Condição NUMERICO exige operador e limite preenchidos.");
        }
        if (!numerico && !semOperadorNemLimite) {
            throw new RegraPrescricaoIncoerenteException(
                    "Condição BOOLEANO não aceita operador nem limite.");
        }
    }
}
