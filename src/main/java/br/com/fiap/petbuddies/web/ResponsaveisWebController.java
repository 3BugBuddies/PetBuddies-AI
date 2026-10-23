package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelRequest;
import br.com.fiap.petbuddies.service.cadastro.ResponsavelService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Cadastro de responsáveis. Responsável não tem flag de ativo: não há exclusão nem desativação aqui;
 * o acesso ao app é do usuário, que não se gerencia por esta tela.
 */
@Controller
@RequestMapping("/responsaveis")
public class ResponsaveisWebController {

    private final ResponsavelService responsavelService;

    public ResponsaveisWebController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @InitBinder("responsavelRequest")
    public void binder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "telefone", new DigitosEditor());
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("responsaveis", responsavelService.listarResumos(q));
        model.addAttribute("q", q);
        return "responsaveis/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("responsavelRequest")) {
            model.addAttribute("responsavelRequest", new ResponsavelRequest());
        }
        model.addAttribute("acaoFormulario", "/responsaveis");
        return "responsaveis/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("responsavelRequest") ResponsavelRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("acaoFormulario", "/responsaveis");
            return "responsaveis/form";
        }
        ResponsavelEntity criado = responsavelService.criar(request);
        redirect.addFlashAttribute("sucesso", "Responsável " + criado.getNome() + " cadastrado(a).");
        return "redirect:/responsaveis";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("responsavelRequest")) {
            model.addAttribute("responsavelRequest", ResponsavelRequest.from(responsavelService.buscarPorId(id)));
        }
        model.addAttribute("responsavelId", id);
        model.addAttribute("acaoFormulario", "/responsaveis/" + id);
        return "responsaveis/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("responsavelRequest") ResponsavelRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("responsavelId", id);
            model.addAttribute("acaoFormulario", "/responsaveis/" + id);
            return "responsaveis/form";
        }
        responsavelService.atualizar(id, request);
        redirect.addFlashAttribute("sucesso", "Cadastro atualizado.");
        return "redirect:/responsaveis";
    }
}
