package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.atendimento.AgendamentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.JanelaLivreDto;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.ProcedimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.RegistroAtendimentoFechamentoRequest;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoProcedimento;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioResponse;
import br.com.fiap.petbuddies.service.cadastro.AnimalService;
import br.com.fiap.petbuddies.service.atendimento.ConsultaService;
import br.com.fiap.petbuddies.exception.atendimento.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaNoPassadoException;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import br.com.fiap.petbuddies.service.atendimento.FechamentoAtendimentoService;
import br.com.fiap.petbuddies.service.atendimento.JanelaAtendimentoService;
import br.com.fiap.petbuddies.service.cadastro.VeterinarioService;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Agenda de consultas. */
@Controller
@RequestMapping("/agenda")
public class AgendaWebController {

    /** Até onde a tela de agendar olha à frente em busca de horário livre. */
    private static final int JANELA_AGENDAR_DIAS = 21;

    private final ConsultaService consultaService;
    private final AnimalService animalService;
    private final VeterinarioService veterinarioService;
    private final FechamentoAtendimentoService fechamentoAtendimentoService;
    private final JanelaAtendimentoService janelaAtendimentoService;

    public AgendaWebController(
            ConsultaService consultaService,
            AnimalService animalService,
            VeterinarioService veterinarioService,
            FechamentoAtendimentoService fechamentoAtendimentoService,
            JanelaAtendimentoService janelaAtendimentoService) {
        this.consultaService = consultaService;
        this.animalService = animalService;
        this.veterinarioService = veterinarioService;
        this.fechamentoAtendimentoService = fechamentoAtendimentoService;
        this.janelaAtendimentoService = janelaAtendimentoService;
    }

    // Os enums entram pelo Model, e nao por T(FQN) no template: assim o compilador
    // enxerga a referencia e um rename de pacote nao passa despercebido.
    @ModelAttribute("tiposConsulta")
    public TipoConsulta[] tiposConsulta() {
        return TipoConsulta.values();
    }

    @ModelAttribute("tiposProcedimento")
    public TipoProcedimento[] tiposProcedimento() {
        return TipoProcedimento.values();
    }

    @GetMapping
    public String semana(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate semana,
            Model model) {
        LocalDate dia = semana == null ? LocalDate.now() : semana;
        model.addAttribute("grade", janelaAtendimentoService.semana(usuario.getVeterinarioId(), dia.with(DayOfWeek.MONDAY)));
        model.addAttribute("hoje", LocalDate.now());
        return "agenda/semana";
    }

    @GetMapping("/novo")
    public String novoForm(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @RequestParam(required = false) Long janelaId,
            Model model) {
        if (!model.containsAttribute("agendamentoRequest")) {
            AgendamentoRequest request = new AgendamentoRequest();
            request.setJanelaId(janelaId);
            model.addAttribute("agendamentoRequest", request);
        }
        prepararAgendar(usuario, model);
        return "agenda/agendar";
    }

    @PostMapping
    public String agendar(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("agendamentoRequest") AgendamentoRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                ConsultaEntity consulta = consultaService.agendar(request);
                redirect.addFlashAttribute("sucesso", "Consulta agendada.");
                return "redirect:/agenda?semana=" + consulta.getDataHora().toLocalDate();
            } catch (JanelaConflitanteException e) {
                result.rejectValue("janelaId", "janela.ocupada", "Esse horário acabou de ser ocupado. Escolha outro.");
            } catch (JanelaNoPassadoException e) {
                result.rejectValue("janelaId", "janela.passou", "Esse horário já passou. Escolha outro.");
            }
        }
        prepararAgendar(usuario, model);
        return "agenda/agendar";
    }

    @GetMapping("/{id}/fechar")
    public String fechar(@PathVariable Long id, Model model) {
        ConsultaEntity consulta = consultaService.buscarPorId(id);
        model.addAttribute("consulta", ConsultaResponse.from(consulta));
        if (!model.containsAttribute("fechamentoRequest")) {
            model.addAttribute("fechamentoRequest", novoFechamentoRequest());
        }
        return "agenda/fechar";
    }

    @PostMapping("/{id}/fechar")
    public String fecharAtendimento(
            @PathVariable Long id,
            @Valid @ModelAttribute("fechamentoRequest") FechamentoAtendimentoRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("consulta", ConsultaResponse.from(consultaService.buscarPorId(id)));
            return "agenda/fechar";
        }
        fechamentoAtendimentoService.fechar(id, request);
        redirect.addFlashAttribute("sucesso", "Atendimento fechado.");
        return "redirect:/agenda";
    }

    // Sem um elemento em cada lista, *{procedimentos[0]....} não resolve no render do GET.
    private FechamentoAtendimentoRequest novoFechamentoRequest() {
        FechamentoAtendimentoRequest request = new FechamentoAtendimentoRequest();
        request.setRegistroAtendimento(new RegistroAtendimentoFechamentoRequest());
        List<ProcedimentoFechamentoRequest> procedimentos = new ArrayList<>();
        procedimentos.add(new ProcedimentoFechamentoRequest());
        request.setProcedimentos(procedimentos);
        List<PrescricaoFechamentoRequest> prescricoes = new ArrayList<>();
        prescricoes.add(new PrescricaoFechamentoRequest());
        request.setPrescricoes(prescricoes);
        return request;
    }

    private void prepararAgendar(UsuarioPrincipal usuario, Model model) {
        LocalDate hoje = LocalDate.now();
        List<JanelaLivreDto> livres = janelaAtendimentoService.listarLivresDaClinica(hoje, hoje.plusDays(JANELA_AGENDAR_DIAS));
        Map<LocalDate, List<JanelaLivreDto>> porDia = livres.stream()
                .collect(Collectors.groupingBy(j -> j.inicio().toLocalDate(), TreeMap::new, Collectors.toList()));
        model.addAttribute("livresPorDia", porDia);
        model.addAttribute("animais", animalService.listar(null, null).stream().map(AnimalResponse::from).toList());
        model.addAttribute("veterinarios", veterinarioService.listar(null).stream()
                .filter(VeterinarioEntity::isAtivo).map(VeterinarioResponse::from).toList());
        model.addAttribute("veterinarioPadrao", usuario.getVeterinarioId());
        model.addAttribute("diasJanela", JANELA_AGENDAR_DIAS);
    }
}
