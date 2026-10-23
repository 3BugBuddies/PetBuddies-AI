package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaRequest;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import br.com.fiap.petbuddies.exception.atendimento.CodigoCondicaoDuplicadoException;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import br.com.fiap.petbuddies.service.atendimento.CondicaoClinicaService;
import br.com.fiap.petbuddies.service.cadastro.VeterinarioService;
import br.com.fiap.petbuddies.web.form.CondicaoForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Catálogo de condições da clínica: o que o check-in observa e o que a receita usa nas regras. */
@Controller
@RequestMapping("/condicoes")
public class CondicoesWebController {

    private final CondicaoClinicaService condicaoService;
    private final VeterinarioService veterinarioService;

    public CondicoesWebController(CondicaoClinicaService condicaoService, VeterinarioService veterinarioService) {
        this.condicaoService = condicaoService;
        this.veterinarioService = veterinarioService;
    }

    @ModelAttribute("tipos")
    public TipoDado[] tipos() {
        return TipoDado.values();
    }

    @ModelAttribute("fontes")
    public TipoFonteValor[] fontes() {
        return TipoFonteValor.values();
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        Long clinicaId = veterinarioService.buscarClinicaId(usuario.getVeterinarioId());
        model.addAttribute("condicoes", condicaoService.listarComUso(clinicaId));
        model.addAttribute("temSinalDeAlarme", condicaoService.temSinalDeAlarme(clinicaId));
        return "condicoes/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        if (!model.containsAttribute("condicaoForm")) {
            model.addAttribute("condicaoForm", new CondicaoForm());
        }
        model.addAttribute("acaoFormulario", "/condicoes");
        return "condicoes/form";
    }

    @PostMapping
    public String criar(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("condicaoForm") CondicaoForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        String codigo = CondicaoClinicaService.gerarCodigo(form.getRotulo());
        if (!result.hasFieldErrors("rotulo") && codigo.isEmpty()) {
            result.rejectValue("rotulo", "codigo.vazio", "Use letras ou números no nome.");
        }
        Long clinicaId = veterinarioService.buscarClinicaId(usuario.getVeterinarioId());
        if (!result.hasErrors()) {
            try {
                condicaoService.criar(pedido(form, codigo, clinicaId, usuario.getVeterinarioId(), true));
                redirect.addFlashAttribute("sucesso", "Condição \"" + form.getRotulo() + "\" criada.");
                return "redirect:/condicoes";
            } catch (CodigoCondicaoDuplicadoException ex) {
                result.rejectValue("rotulo", "codigo.duplicado", "Já existe uma condição com esse nome.");
            }
        }
        model.addAttribute("acaoFormulario", "/condicoes");
        return "condicoes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioPrincipal usuario, @PathVariable Long id, Model model) {
        CondicaoClinicaResponse condicao = daClinica(usuario, id);
        if (!model.containsAttribute("condicaoForm")) {
            model.addAttribute("condicaoForm", CondicaoForm.from(condicao));
        }
        carregarEdicao(model, condicao);
        return "condicoes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @PathVariable Long id,
            @Valid @ModelAttribute("condicaoForm") CondicaoForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        CondicaoClinicaResponse atual = daClinica(usuario, id);
        // O código nunca muda na edição: regras e relatos antigos referem a condição por ele.
        if (condicaoService.emUso(id)) {
            form.setTipoDado(atual.getTipoDado());
        }
        if (!result.hasErrors()) {
            try {
                condicaoService.atualizar(id, pedido(form, atual.getCodigo(), atual.getClinicaId(),
                        atual.getVeterinarioAutorId(), atual.getAtivo()));
                redirect.addFlashAttribute("sucesso", "Condição atualizada.");
                return "redirect:/condicoes";
            } catch (CodigoCondicaoDuplicadoException ex) {
                result.rejectValue("rotulo", "codigo.duplicado", "Já existe uma condição com esse nome.");
            }
        }
        carregarEdicao(model, atual);
        return "condicoes/form";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@AuthenticationPrincipal UsuarioPrincipal usuario, @PathVariable Long id, RedirectAttributes redirect) {
        daClinica(usuario, id);
        condicaoService.alterarSituacao(id, false);
        redirect.addFlashAttribute("sucesso", "Condição desativada. Regras e relatos antigos continuam como estavam.");
        return "redirect:/condicoes";
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@AuthenticationPrincipal UsuarioPrincipal usuario, @PathVariable Long id, RedirectAttributes redirect) {
        daClinica(usuario, id);
        condicaoService.alterarSituacao(id, true);
        redirect.addFlashAttribute("sucesso", "Condição reativada.");
        return "redirect:/condicoes";
    }

    private CondicaoClinicaResponse daClinica(UsuarioPrincipal usuario, Long id) {
        return condicaoService.buscarDaClinica(id, veterinarioService.buscarClinicaId(usuario.getVeterinarioId()));
    }

    private void carregarEdicao(Model model, CondicaoClinicaResponse condicao) {
        model.addAttribute("condicaoId", condicao.getId());
        model.addAttribute("codigoAtual", condicao.getCodigo());
        model.addAttribute("tipoTravado", condicaoService.emUso(condicao.getId()));
        model.addAttribute("acaoFormulario", "/condicoes/" + condicao.getId());
    }

    private CondicaoClinicaRequest pedido(CondicaoForm form, String codigo, Long clinicaId, Long autorId, boolean ativo) {
        boolean numerica = form.getTipoDado() == TipoDado.NUMERICO;
        return new CondicaoClinicaRequest(codigo, form.getRotulo().trim(), form.getTipoDado(), form.getFonteValor(),
                numerica && form.getUnidade() != null ? form.getUnidade().trim() : null, form.isCritica(), ativo, clinicaId, autorId);
    }
}
