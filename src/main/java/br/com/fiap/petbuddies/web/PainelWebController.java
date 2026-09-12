package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.dto.cadastro.ClinicaResponse;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.service.cadastro.ClinicaService;
import br.com.fiap.petbuddies.service.atendimento.ConsultaService;
import br.com.fiap.petbuddies.service.cuidado.AcompanhamentoService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Painel do vet: o acompanhamento do cuidado e as consultas do dia. Só leitura. */
@Controller
@RequestMapping("/painel")
public class PainelWebController {

    /** Quantas linhas de "precisa de atenção" cabem antes de a lista virar relatório. */
    private static final int LIMITE_VENCIDOS = 8;

    private final ClinicaService clinicaService;
    private final ConsultaService consultaService;
    private final AcompanhamentoService acompanhamentoService;

    public PainelWebController(
            ClinicaService clinicaService,
            ConsultaService consultaService,
            AcompanhamentoService acompanhamentoService) {
        this.clinicaService = clinicaService;
        this.consultaService = consultaService;
        this.acompanhamentoService = acompanhamentoService;
    }

    @GetMapping
    public String painel(Model model) {
        LocalDate hoje = LocalDate.now();
        List<ConsultaResponse> consultasHoje = consultaService.listar(null, null).stream()
                .filter(c -> c.getDataHora() != null && c.getDataHora().toLocalDate().isEqual(hoje))
                .map(ConsultaResponse::from)
                .toList();

        model.addAttribute("clinica", clinicaService.buscarUnica().map(ClinicaResponse::from).orElse(null));
        model.addAttribute("hoje", hoje);
        model.addAttribute("resumo", acompanhamentoService.resumo());
        model.addAttribute("vencidos", acompanhamentoService.vencidos(LIMITE_VENCIDOS));
        model.addAttribute("consultasHoje", consultasHoje);
        return "painel";
    }
}
