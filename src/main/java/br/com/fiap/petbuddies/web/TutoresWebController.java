package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.dto.ResponsavelRequest;
import br.com.fiap.petbuddies.dto.ResponsavelResponse;
import br.com.fiap.petbuddies.service.ResponsavelService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Cadastro de tutores (responsáveis). */
@Controller
@RequestMapping("/tutores")
public class TutoresWebController {

    private final ResponsavelService responsavelService;

    public TutoresWebController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @GetMapping
    public String listar(Model model) {
        List<ResponsavelResponse> tutores = responsavelService.listar(null).stream()
                .map(ResponsavelResponse::from)
                .toList();
        model.addAttribute("tutores", tutores);
        return "tutores/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("responsavelRequest")) {
            model.addAttribute("responsavelRequest", new ResponsavelRequest());
        }
        model.addAttribute("acaoFormulario", "/tutores");
        return "tutores/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("responsavelRequest") ResponsavelRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("acaoFormulario", "/tutores");
            return "tutores/form";
        }
        ResponsavelEntity criado = responsavelService.criar(request);
        redirect.addFlashAttribute("sucesso", "Tutor(a) " + criado.getNome() + " cadastrado(a).");
        return "redirect:/tutores";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("responsavelRequest")) {
            model.addAttribute("responsavelRequest", paraRequest(responsavelService.buscarPorId(id)));
        }
        model.addAttribute("responsavelId", id);
        model.addAttribute("acaoFormulario", "/tutores/" + id);
        return "tutores/form";
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
            model.addAttribute("acaoFormulario", "/tutores/" + id);
            return "tutores/form";
        }
        responsavelService.atualizar(id, request);
        redirect.addFlashAttribute("sucesso", "Cadastro atualizado.");
        return "redirect:/tutores";
    }

    private ResponsavelRequest paraRequest(ResponsavelEntity entity) {
        ResponsavelRequest request = new ResponsavelRequest();
        request.setNome(entity.getNome());
        request.setTelefone(entity.getTelefone());
        request.setEmail(entity.getEmail());
        return request;
    }
}
