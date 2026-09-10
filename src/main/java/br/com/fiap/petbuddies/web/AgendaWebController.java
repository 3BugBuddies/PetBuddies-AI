package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.dto.AnimalResponse;
import br.com.fiap.petbuddies.dto.ConsultaRequest;
import br.com.fiap.petbuddies.dto.ConsultaResponse;
import br.com.fiap.petbuddies.dto.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.PrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.dto.ProcedimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.RegistroAtendimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.VeterinarioResponse;
import br.com.fiap.petbuddies.service.AnimalService;
import br.com.fiap.petbuddies.service.ConsultaService;
import br.com.fiap.petbuddies.service.FechamentoAtendimentoService;
import br.com.fiap.petbuddies.service.VeterinarioService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Agenda de consultas. */
@Controller
@RequestMapping("/agenda")
public class AgendaWebController {

    private final ConsultaService consultaService;
    private final AnimalService animalService;
    private final VeterinarioService veterinarioService;
    private final FechamentoAtendimentoService fechamentoAtendimentoService;

    public AgendaWebController(
            ConsultaService consultaService,
            AnimalService animalService,
            VeterinarioService veterinarioService,
            FechamentoAtendimentoService fechamentoAtendimentoService) {
        this.consultaService = consultaService;
        this.animalService = animalService;
        this.veterinarioService = veterinarioService;
        this.fechamentoAtendimentoService = fechamentoAtendimentoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<ConsultaResponse> consultas = consultaService.listar(null, null).stream()
                .map(ConsultaResponse::from)
                .toList();
        model.addAttribute("consultas", consultas);
        model.addAttribute("nomesAnimais", mapaNomes(animalService.listar(null, null), AnimalEntity::getId, AnimalEntity::getNome));
        model.addAttribute("nomesVeterinarios",
                mapaNomes(veterinarioService.listar(null), VeterinarioEntity::getId, VeterinarioEntity::getNome));
        return "agenda/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("consultaRequest")) {
            model.addAttribute("consultaRequest", new ConsultaRequest());
        }
        model.addAttribute("animais", listaAnimais());
        model.addAttribute("veterinarios", listaVeterinarios());
        return "agenda/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("consultaRequest") ConsultaRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("animais", listaAnimais());
            model.addAttribute("veterinarios", listaVeterinarios());
            return "agenda/form";
        }
        consultaService.criar(request);
        redirect.addFlashAttribute("sucesso", "Consulta agendada.");
        return "redirect:/agenda";
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

    private List<AnimalResponse> listaAnimais() {
        return animalService.listar(null, null).stream().map(AnimalResponse::from).toList();
    }

    private List<VeterinarioResponse> listaVeterinarios() {
        return veterinarioService.listar(null).stream().map(VeterinarioResponse::from).toList();
    }

    private <T> Map<Long, String> mapaNomes(List<T> itens, Function<T, Long> id, Function<T, String> nome) {
        return itens.stream().collect(Collectors.toMap(id, nome));
    }
}
