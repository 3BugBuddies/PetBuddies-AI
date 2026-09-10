package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaResponse;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.service.AnimalService;
import br.com.fiap.petbuddies.service.ClinicaService;
import br.com.fiap.petbuddies.service.ConsultaService;
import br.com.fiap.petbuddies.service.ResponsavelService;
import br.com.fiap.petbuddies.service.VeterinarioService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Painel do vet: números da clínica e as consultas do dia. Não é fluxo — só leitura. */
@Controller
@RequestMapping("/painel")
public class PainelWebController {

    private final ClinicaService clinicaService;
    private final VeterinarioService veterinarioService;
    private final ResponsavelService responsavelService;
    private final AnimalService animalService;
    private final ConsultaService consultaService;

    public PainelWebController(
            ClinicaService clinicaService,
            VeterinarioService veterinarioService,
            ResponsavelService responsavelService,
            AnimalService animalService,
            ConsultaService consultaService) {
        this.clinicaService = clinicaService;
        this.veterinarioService = veterinarioService;
        this.responsavelService = responsavelService;
        this.animalService = animalService;
        this.consultaService = consultaService;
    }

    @GetMapping
    public String painel(Model model) {
        List<ClinicaEntity> clinicas = clinicaService.listar();
        LocalDate hoje = LocalDate.now();
        List<ConsultaResponse> consultasHoje = consultaService.listar(null, null).stream()
                .filter(c -> c.getDataHora() != null && c.getDataHora().toLocalDate().isEqual(hoje))
                .map(ConsultaResponse::from)
                .toList();

        ClinicaResponse clinica = clinicas.isEmpty() ? null : ClinicaResponse.from(clinicas.get(0));
        model.addAttribute("clinica", clinica);
        model.addAttribute("totalVeterinarios", veterinarioService.listar(null).size());
        model.addAttribute("totalTutores", responsavelService.listar(null).size());
        model.addAttribute("totalPacientes", animalService.listar(null, null).size());
        model.addAttribute("consultasHoje", consultasHoje);
        return "painel";
    }
}
