package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.enums.atendimento.TipoProcedimento;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoResponse;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloResponse;
import br.com.fiap.petbuddies.exception.atendimento.ItemPlanoInvalidoParaProcedimentoException;
import br.com.fiap.petbuddies.exception.cuidado.DuracaoTratamentoExcedeTetoException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoIncoerenteException;
import br.com.fiap.petbuddies.service.atendimento.FechamentoPainelService;
import br.com.fiap.petbuddies.service.atendimento.FechamentoPainelService.PreparoFechamento;
import br.com.fiap.petbuddies.web.form.FechamentoEstado;
import br.com.fiap.petbuddies.web.form.FechamentoEstado.CondicaoOpcao;
import br.com.fiap.petbuddies.web.form.FechamentoEstado.ItemAberto;
import br.com.fiap.petbuddies.web.form.FechamentoEstado.MedicamentoEstado;
import br.com.fiap.petbuddies.web.form.FechamentoEstado.ReceitaOpcao;
import br.com.fiap.petbuddies.web.form.FechamentoEstado.RegraEstado;
import br.com.fiap.petbuddies.web.form.FechamentoPainelForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Fechar atendimento em quatro etapas: registro, procedimento, prescrição e revisão, com um único POST no fim. */
@Controller
@RequestMapping("/agenda/{consultaId}/fechar")
public class FechamentoWebController {

    private static final Pattern MEDICAMENTO = Pattern.compile("^prescricoes\\[(\\d+)\\]\\.");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FechamentoPainelService fechamentoPainelService;
    private final Validator validator;

    public FechamentoWebController(FechamentoPainelService fechamentoPainelService, Validator validator) {
        this.fechamentoPainelService = fechamentoPainelService;
        this.validator = validator;
    }

    @ModelAttribute("tiposProcedimento")
    public TipoProcedimento[] tiposProcedimento() {
        return TipoProcedimento.values();
    }

    @ModelAttribute("operadores")
    public OperadorRegra[] operadores() {
        return OperadorRegra.values();
    }

    @ModelAttribute("acoes")
    public TipoAcaoRegra[] acoes() {
        return TipoAcaoRegra.values();
    }

    @GetMapping
    public String abrir(@PathVariable Long consultaId, Model model) {
        PreparoFechamento preparo = fechamentoPainelService.preparar(consultaId);
        FechamentoPainelForm form = new FechamentoPainelForm();
        // O padrão é a hora da consulta: o atendimento já aconteceu nela, e fechar mais tarde não a muda.
        LocalDateTime daConsulta = preparo.consulta().getDataHora().truncatedTo(ChronoUnit.MINUTES);
        form.getRegistro().setDataAtendimento(daConsulta);
        form.getRegistro().setPeso(preparo.pesoAtual());
        form.getProcedimento().setData(daConsulta);
        form.getPrescricoes().get(0).setDataInicio(LocalDate.now());
        carregar(model, preparo, form, 1, null);
        model.addAttribute("fechamentoForm", form);
        return "agenda/fechar";
    }

    @PostMapping
    public String fechar(
            @PathVariable Long consultaId,
            @ModelAttribute("fechamentoForm") FechamentoPainelForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        FechamentoAtendimentoRequest request = form.paraRequest();
        validar(request, result);
        rejeitarRepetidos(request, result);

        if (!result.hasErrors()) {
            try {
                FechamentoAtendimentoResponse fechado = fechamentoPainelService.fechar(consultaId, request);
                redirect.addFlashAttribute("sucesso", "Atendimento fechado.");
                return "redirect:/pacientes/" + fechado.getRegistroAtendimento().getAnimalId();
            } catch (ItemPlanoInvalidoParaProcedimentoException ex) {
                result.reject("item.invalido", ex.getMessage());
                return reabrir(consultaId, form, result, model, 2);
            } catch (RegraPrescricaoIncoerenteException | DuracaoTratamentoExcedeTetoException ex) {
                result.reject("prescricao.invalida", ex.getMessage());
                return reabrir(consultaId, form, result, model, 3);
            }
        }
        return reabrir(consultaId, form, result, model, primeiraEtapaComErro(result));
    }

    private String reabrir(Long consultaId, FechamentoPainelForm form, BindingResult result, Model model, int etapa) {
        carregar(model, fechamentoPainelService.preparar(consultaId), form, etapa, result);
        return "agenda/fechar";
    }

    // Valida o request montado, e não o form: seção desligada não pode reclamar de campo vazio.
    private void validar(FechamentoAtendimentoRequest request, BindingResult result) {
        Set<ConstraintViolation<FechamentoAtendimentoRequest>> violacoes = validator.validate(request);
        for (ConstraintViolation<FechamentoAtendimentoRequest> v : violacoes) {
            String campo = FechamentoPainelForm.campoDoFormulario(v.getPropertyPath().toString());
            if (campo == null) {
                result.addError(new ObjectError(result.getObjectName(), v.getMessage()));
            } else {
                result.rejectValue(campo, "invalido", comMedicamento(campo, v.getMessage(), request));
            }
        }
    }

    // Mesmo medicamento duas vezes na receita (sem diferenciar maiúsculas): o erro cai no bloco repetido.
    private void rejeitarRepetidos(FechamentoAtendimentoRequest request, BindingResult result) {
        Set<String> vistos = new java.util.HashSet<>();
        for (int i = 0; i < request.getPrescricoes().size(); i++) {
            String nome = request.getPrescricoes().get(i).getMedicamento();
            if (nome != null && !nome.isBlank() && !vistos.add(nome.trim().toLowerCase())) {
                result.rejectValue("prescricoes[" + i + "].medicamento", "repetido",
                        "Medicamento " + (i + 1) + ": já consta na receita.");
            }
        }
    }

    // Com mais de um medicamento a mensagem diz qual; com um só, fica como sempre foi.
    private static String comMedicamento(String campo, String mensagem, FechamentoAtendimentoRequest request) {
        Matcher m = MEDICAMENTO.matcher(campo);
        if (!m.find() || request.getPrescricoes().size() < 2) {
            return mensagem;
        }
        return "Medicamento " + (Integer.parseInt(m.group(1)) + 1) + ": " + mensagem;
    }

    private int primeiraEtapaComErro(BindingResult result) {
        int primeira = 4;
        for (var erro : result.getFieldErrors()) {
            primeira = Math.min(primeira, FechamentoPainelForm.etapaDoCampo(erro.getField()));
        }
        return primeira;
    }

    private void carregar(Model model, PreparoFechamento preparo, FechamentoPainelForm form, int etapa,
                          BindingResult result) {
        model.addAttribute("consulta", preparo.consulta());
        model.addAttribute("animalNome", preparo.animalNome());
        model.addAttribute("condicoes", preparo.condicoes());
        model.addAttribute("receitasModelo", preparo.receitasModelo());
        model.addAttribute("estado", estado(preparo, form, etapa, result));
    }

    private FechamentoEstado estado(PreparoFechamento preparo, FechamentoPainelForm form, int etapa,
                                    BindingResult result) {
        List<ItemAberto> itens = preparo.itensAbertos().stream()
                .map(i -> new ItemAberto(String.valueOf(i.getId()), i.getNome(), i.getTipo(),
                        i.getDataAlvo().format(DIA), i.isVencido()))
                .toList();
        List<CondicaoOpcao> condicoes = preparo.condicoes().stream()
                .map(c -> new CondicaoOpcao(String.valueOf(c.getId()), c.getRotulo(), c.getTipoDado().name(),
                        c.getUnidade(), Boolean.TRUE.equals(c.getCritica())))
                .toList();
        List<MedicamentoEstado> medicamentos = new ArrayList<>();
        for (int i = 0; i < form.getPrescricoes().size(); i++) {
            medicamentos.add(medicamento(form.getPrescricoes().get(i), errosDoMedicamento(result, i)));
        }
        List<ReceitaOpcao> receitasModelo = preparo.receitasModelo().stream().map(FechamentoWebController::opcao).toList();
        var procedimento = form.getProcedimento();
        return new FechamentoEstado(etapa, form.isHouveProcedimento(), form.isPrescrever(),
                procedimento.getTipo() == null ? TipoProcedimento.VACINACAO.name() : procedimento.getTipo().name(),
                procedimento.getItemPlanoId() == null ? "" : procedimento.getItemPlanoId().toString(),
                procedimento.getNome() == null ? "" : procedimento.getNome(),
                itens, condicoes, medicamentos,
                mapa(TipoProcedimento.values(), TipoProcedimento::getRotulo),
                mapa(OperadorRegra.values(), OperadorRegra::getRotulo),
                mapa(TipoAcaoRegra.values(), TipoAcaoRegra::getRotulo),
                receitasModelo);
    }

    private static List<String> errosDoMedicamento(BindingResult result, int indice) {
        if (result == null) {
            return List.of();
        }
        String prefixo = "prescricoes[" + indice + "].";
        return result.getFieldErrors().stream()
                .filter(e -> e.getField().startsWith(prefixo))
                .map(e -> e.getDefaultMessage()).toList();
    }

    private static MedicamentoEstado medicamento(PrescricaoFechamentoRequest p, List<String> erros) {
        List<RegraEstado> regras = p.getRegras() == null ? List.of() : p.getRegras().stream()
                .map(r -> new RegraEstado(
                        r.getCondicaoClinicaId() == null ? "" : r.getCondicaoClinicaId().toString(),
                        r.getOperador() == null ? "" : r.getOperador().name(),
                        r.getLimite() == null ? "" : r.getLimite().toPlainString(),
                        r.getAcaoDose() == null ? "" : r.getAcaoDose().name()))
                .toList();
        return new MedicamentoEstado(texto(p.getMedicamento()), texto(p.getDoseMin()), texto(p.getDoseMax()),
                texto(p.getUnidade()), texto(p.getFrequenciaDia()), texto(p.getDuracaoDias()),
                texto(p.getDataInicio()), texto(p.getOrientacao()), texto(p.getReceitaModeloId()),
                texto(p.getReceitaModeloVersao()), regras, erros);
    }

    private static String texto(Object valor) {
        return valor == null ? "" : valor instanceof BigDecimal n ? n.toPlainString() : valor.toString();
    }

    private static ReceitaOpcao opcao(ReceitaModeloResponse r) {
        List<RegraEstado> regras = r.getRegras().stream()
                .map(g -> new RegraEstado(
                        g.getCondicaoClinicaId().toString(),
                        g.getOperador() == null ? "" : g.getOperador().name(),
                        g.getLimite() == null ? "" : g.getLimite().stripTrailingZeros().toPlainString(),
                        g.getAcaoDose().name()))
                .toList();
        return new ReceitaOpcao(r.getId().toString(), r.getVersao().toString(), r.getTitulo(), r.getMedicamento(),
                r.getDoseMin().toPlainString(), r.getDoseMax().toPlainString(), r.getUnidade(),
                r.getFrequenciaDia().toString(), r.getDuracaoDias().toString(),
                r.getOrientacao() == null ? "" : r.getOrientacao(), regras);
    }

    private <E extends Enum<E>> Map<String, String> mapa(E[] valores, Function<E, String> rotulo) {
        Map<String, String> rotulado = new LinkedHashMap<>();
        Arrays.stream(valores).forEach(v -> rotulado.put(v.name(), rotulo.apply(v)));
        return rotulado;
    }
}
