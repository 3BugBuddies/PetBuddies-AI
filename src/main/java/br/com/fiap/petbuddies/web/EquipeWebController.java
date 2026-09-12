package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioRequest;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioResponse;
import br.com.fiap.petbuddies.service.cadastro.ClinicaService;
import br.com.fiap.petbuddies.service.cadastro.VeterinarioService;
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

/** Cadastro da equipe clínica (veterinários). Só a clínica única do produto, sem seleção de clínica na tela. */
@Controller
@RequestMapping("/equipe")
public class EquipeWebController {

    private final VeterinarioService veterinarioService;
    private final ClinicaService clinicaService;

    public EquipeWebController(VeterinarioService veterinarioService, ClinicaService clinicaService) {
        this.veterinarioService = veterinarioService;
        this.clinicaService = clinicaService;
    }

    @GetMapping
    public String listar(Model model) {
        List<VeterinarioResponse> equipe = veterinarioService.listar(null).stream()
                .map(VeterinarioResponse::from)
                .toList();
        model.addAttribute("equipe", equipe);
        return "equipe/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("veterinarioRequest")) {
            VeterinarioRequest request = new VeterinarioRequest();
            request.setClinicaId(clinicaService.buscarUnica().map(ClinicaEntity::getId).orElse(null));
            model.addAttribute("veterinarioRequest", request);
        }
        model.addAttribute("acaoFormulario", "/equipe");
        return "equipe/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("veterinarioRequest") VeterinarioRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("acaoFormulario", "/equipe");
            return "equipe/form";
        }
        VeterinarioEntity criado = veterinarioService.criar(request);
        redirect.addFlashAttribute("sucesso", "Veterinário(a) " + criado.getNome() + " cadastrado(a).");
        return "redirect:/equipe";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("veterinarioRequest")) {
            model.addAttribute("veterinarioRequest", paraRequest(veterinarioService.buscarPorId(id)));
        }
        model.addAttribute("veterinarioId", id);
        model.addAttribute("acaoFormulario", "/equipe/" + id);
        return "equipe/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("veterinarioRequest") VeterinarioRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("veterinarioId", id);
            model.addAttribute("acaoFormulario", "/equipe/" + id);
            return "equipe/form";
        }
        veterinarioService.atualizar(id, request);
        redirect.addFlashAttribute("sucesso", "Cadastro atualizado.");
        return "redirect:/equipe";
    }

    private VeterinarioRequest paraRequest(VeterinarioEntity entity) {
        VeterinarioRequest request = new VeterinarioRequest();
        request.setNome(entity.getNome());
        request.setCrmv(entity.getCrmv());
        request.setEmail(entity.getEmail());
        request.setAtivo(entity.isAtivo());
        request.setClinicaId(entity.getClinica().getId());
        return request;
    }
}
