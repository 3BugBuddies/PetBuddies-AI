package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaAgendaDto;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaResponse;
import br.com.fiap.petbuddies.dto.checkin.RelatoParaLerDto;
import br.com.fiap.petbuddies.dto.checkin.RelatoParaLerDto.Tipo;
import br.com.fiap.petbuddies.service.atendimento.ConsultaService;
import br.com.fiap.petbuddies.service.cadastro.ClinicaService;
import br.com.fiap.petbuddies.service.checkin.RelatoPainelService;
import br.com.fiap.petbuddies.service.cuidado.AcompanhamentoService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Início do painel: o que precisa da veterinária hoje. Só leitura. */
@Controller
@RequestMapping("/painel")
public class PainelWebController {

    private static final Duration JANELA_RELATOS = Duration.ofHours(48);

    private final ClinicaService clinicaService;
    private final ConsultaService consultaService;
    private final AcompanhamentoService acompanhamentoService;
    private final RelatoPainelService relatoPainelService;
    private final Formatador formatador;

    public PainelWebController(
            ClinicaService clinicaService,
            ConsultaService consultaService,
            AcompanhamentoService acompanhamentoService,
            RelatoPainelService relatoPainelService,
            Formatador formatador) {
        this.clinicaService = clinicaService;
        this.consultaService = consultaService;
        this.acompanhamentoService = acompanhamentoService;
        this.relatoPainelService = relatoPainelService;
        this.formatador = formatador;
    }

    @GetMapping
    public String inicio(Model model) {
        LocalDateTime agora = LocalDateTime.now();
        List<ConsultaAgendaDto> consultasHoje = consultaService.agendaDoDia(agora.toLocalDate());
        List<RelatoParaLerDto> relatos = relatoPainelService.paraLer(JANELA_RELATOS);
        List<RelatoParaLerDto> checkinsHoje = relatoPainelService.doDia(agora.toLocalDate());

        model.addAttribute("clinica", clinicaService.buscarUnica().map(ClinicaResponse::from).orElse(null));
        model.addAttribute("agora", agora);
        model.addAttribute("consultasHoje", consultasHoje);
        model.addAttribute("atendidasHoje",
                consultasHoje.stream().filter(c -> c.status() == StatusConsulta.REALIZADA).count());
        model.addAttribute("relatos", relatos);
        model.addAttribute("alarmes", relatos.stream().filter(r -> r.tipo() == Tipo.ALARME).count());
        model.addAttribute("semRegra", relatos.stream().filter(r -> r.tipo() == Tipo.SEM_REGRA).count());
        model.addAttribute("checkinsHoje", checkinsHoje.size());
        model.addAttribute("checkinsHoraResumo", checkinsHoje.stream()
                .limit(3)
                .map(c -> c.animalNome() + " às " + formatador.hora(c.registradoEm()))
                .collect(Collectors.joining(", ")));
        model.addAttribute("resumo", acompanhamentoService.resumo());
        return "painel";
    }
}
