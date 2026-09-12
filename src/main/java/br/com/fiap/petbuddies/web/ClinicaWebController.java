package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaRequest;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.service.cadastro.ClinicaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** A clínica é única no produto: visualizar e editar, sem lista. */
@Controller
@RequestMapping("/clinica")
public class ClinicaWebController {

    private final ClinicaService clinicaService;

    public ClinicaWebController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    @GetMapping
    public String editarForm(Model model) {
        ClinicaEntity clinica = clinicaUnica();
        model.addAttribute("clinicaId", clinica.getId());
        if (!model.containsAttribute("clinicaRequest")) {
            model.addAttribute("clinicaRequest", paraRequest(clinica));
        }
        return "clinica/form";
    }

    @PostMapping
    public String atualizar(
            @Valid @ModelAttribute("clinicaRequest") ClinicaRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        ClinicaEntity clinica = clinicaUnica();
        if (result.hasErrors()) {
            model.addAttribute("clinicaId", clinica.getId());
            return "clinica/form";
        }
        clinicaService.atualizar(clinica.getId(), request);
        redirect.addFlashAttribute("sucesso", "Dados da clínica atualizados.");
        return "redirect:/clinica";
    }

    private ClinicaEntity clinicaUnica() {
        return clinicaService.buscarUnica().orElseThrow(ClinicaNaoEncontradaException::new);
    }

    private ClinicaRequest paraRequest(ClinicaEntity entity) {
        ClinicaRequest request = new ClinicaRequest();
        request.setNome(entity.getNome());
        request.setCnpj(entity.getCnpj());
        request.setTelefone(entity.getTelefone());
        request.setEmail(entity.getEmail());
        return request;
    }
}
