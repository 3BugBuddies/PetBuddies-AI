package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusProcedimento;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.ProcedimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.FechamentoAtendimentoResponse;
import br.com.fiap.petbuddies.dto.PrescricaoComRegrasResponse;
import br.com.fiap.petbuddies.dto.PrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.dto.ProcedimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.RegistroAtendimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.RegraPrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoIncoerenteException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fecha um atendimento num único ato transacional: grava o registro, os
 * procedimentos executados e as prescrições assinadas com suas regras
 * condicionais, e muda a consulta para REALIZADA. Ou tudo grava, ou nada
 * grava.
 *
 * <p>Reusa os mesmos repositórios que {@code RegistroAtendimentoService},
 * {@code ProcedimentoService}, {@code PrescricaoService} e {@code
 * RegraPrescricaoService} usam. A validação de coerência da regra é
 * espelhada de {@code RegraPrescricaoService} porque aqui a prescrição
 * ainda não tem id para passar pelo {@code RegraPrescricaoRequest} avulso.</p>
 */
@Service
public class FechamentoAtendimentoService {

    private final ConsultaService consultaService;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final RegraPrescricaoRepository regraPrescricaoRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;

    public FechamentoAtendimentoService(
            ConsultaService consultaService,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            ProcedimentoRepository procedimentoRepository,
            PrescricaoRepository prescricaoRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository) {
        this.consultaService = consultaService;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.regraPrescricaoRepository = regraPrescricaoRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
    }

    @Transactional
    public FechamentoAtendimentoResponse fechar(Long consultaId, FechamentoAtendimentoRequest request) {
        // Primeiro passo, dentro da mesma transação: valida o estado e já marca
        // REALIZADA. Se algo adiante falhar, o rollback desfaz esta mudança
        // também — não há como a consulta ficar REALIZADA sem o resto gravado.
        ConsultaEntity consulta = consultaService.fechar(consultaId);
        AnimalEntity animal = consulta.getAnimal();
        VeterinarioEntity veterinario = consulta.getVeterinario();

        RegistroAtendimentoEntity registro = criarRegistro(request.getRegistroAtendimento(), animal, consulta);
        List<ProcedimentoEntity> procedimentos = criarProcedimentos(request.getProcedimentos(), registro, animal, veterinario);
        List<PrescricaoComRegrasResponse> prescricoes = criarPrescricoes(request.getPrescricoes(), registro, animal, veterinario);

        return FechamentoAtendimentoResponse.from(consulta, registro, procedimentos, prescricoes);
    }

    private RegistroAtendimentoEntity criarRegistro(
            RegistroAtendimentoFechamentoRequest r, AnimalEntity animal, ConsultaEntity consulta) {
        RegistroAtendimentoEntity registro = new RegistroAtendimentoEntity();
        registro.setDataAtendimento(r.getDataAtendimento());
        registro.setAnamnese(r.getAnamnese());
        registro.setDiagnostico(r.getDiagnostico());
        registro.setTratamento(r.getTratamento());
        registro.setObservacao(r.getObservacao());
        registro.setProximoRetorno(r.getProximoRetorno());
        registro.setProximaVacina(r.getProximaVacina());
        registro.setAnimal(animal);
        registro.setConsulta(consulta);
        return registroAtendimentoRepository.save(registro);
    }

    private List<ProcedimentoEntity> criarProcedimentos(
            List<ProcedimentoFechamentoRequest> requests, RegistroAtendimentoEntity registro,
            AnimalEntity animal, VeterinarioEntity veterinario) {
        List<ProcedimentoEntity> procedimentos = new ArrayList<>();
        for (ProcedimentoFechamentoRequest p : nullSafe(requests)) {
            ProcedimentoEntity entity = new ProcedimentoEntity();
            entity.setTipo(p.getTipo());
            entity.setNome(p.getNome());
            entity.setDescricao(p.getDescricao());
            // Nasce REALIZADO: um procedimento que chega pelo fechamento já aconteceu.
            entity.setStatus(StatusProcedimento.REALIZADO);
            entity.setDataPrevistaInicio(p.getDataPrevistaInicio());
            entity.setDataPrevistaFim(p.getDataPrevistaFim());
            entity.setAnexosUrl(p.getAnexosUrl());
            entity.setObservacao(p.getObservacao());
            entity.setRegistroAtendimento(registro);
            entity.setAnimal(animal);
            entity.setVeterinario(veterinario);
            procedimentos.add(procedimentoRepository.save(entity));
        }
        return procedimentos;
    }

    private List<PrescricaoComRegrasResponse> criarPrescricoes(
            List<PrescricaoFechamentoRequest> requests, RegistroAtendimentoEntity registro,
            AnimalEntity animal, VeterinarioEntity veterinario) {
        List<PrescricaoComRegrasResponse> resultado = new ArrayList<>();
        for (PrescricaoFechamentoRequest pr : nullSafe(requests)) {
            PrescricaoEntity prescricao = new PrescricaoEntity();
            prescricao.setMedicamento(pr.getMedicamento());
            prescricao.setDoseMin(pr.getDoseMin());
            prescricao.setDoseMax(pr.getDoseMax());
            prescricao.setUnidade(pr.getUnidade());
            prescricao.setFrequenciaDia(pr.getFrequenciaDia());
            prescricao.setDuracaoDias(pr.getDuracaoDias());
            prescricao.setDataInicio(pr.getDataInicio());
            prescricao.setOrientacao(pr.getOrientacao());
            prescricao.setMaterialOrigemId(pr.getMaterialOrigemId());
            prescricao.setVersaoOrigem(pr.getVersaoOrigem());
            prescricao.setAnimal(animal);
            prescricao.setVeterinario(veterinario);
            prescricao.setRegistroAtendimento(registro);
            prescricao = prescricaoRepository.save(prescricao);

            List<RegraPrescricaoEntity> regras = criarRegras(pr.getRegras(), prescricao);
            resultado.add(PrescricaoComRegrasResponse.from(prescricao, regras));
        }
        return resultado;
    }

    private List<RegraPrescricaoEntity> criarRegras(List<RegraPrescricaoFechamentoRequest> requests, PrescricaoEntity prescricao) {
        List<RegraPrescricaoEntity> regras = new ArrayList<>();
        for (RegraPrescricaoFechamentoRequest rg : nullSafe(requests)) {
            CondicaoClinicaEntity condicao = condicaoClinicaRepository.findById(rg.getCondicaoClinicaId())
                    .orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(rg.getCondicaoClinicaId()));
            validarCoerencia(condicao.getTipoDado(), rg.getOperador(), rg.getLimite());

            RegraPrescricaoEntity regra = new RegraPrescricaoEntity();
            regra.setPrescricao(prescricao);
            regra.setCondicaoClinica(condicao);
            // A cópia congelada: o que foi assinado, não o que o catálogo diz hoje.
            regra.setRotuloCongelado(condicao.getRotulo());
            regra.setTipoDadoCongelado(condicao.getTipoDado());
            regra.setFonteValorCongelada(condicao.getFonteValor());
            regra.setOperador(rg.getOperador());
            regra.setLimite(rg.getLimite());
            regra.setAcaoDose(rg.getAcaoDose());
            regra.setOrdem(rg.getOrdem());
            regras.add(regraPrescricaoRepository.save(regra));
        }
        return regras;
    }

    // CK_REGRA_COERENCIA, espelhado de RegraPrescricaoService: NUMERICO exige
    // operador e limite; BOOLEANO não aceita nenhum dos dois.
    private void validarCoerencia(TipoDado tipoDado, OperadorRegra operador, BigDecimal limite) {
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

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
