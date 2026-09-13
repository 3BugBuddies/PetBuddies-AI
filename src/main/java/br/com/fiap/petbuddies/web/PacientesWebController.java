package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Porte;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.dto.cadastro.AnimalRequest;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoResponse;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelResponse;
import br.com.fiap.petbuddies.service.cadastro.AnimalService;
import br.com.fiap.petbuddies.service.atendimento.ConsultaService;
import br.com.fiap.petbuddies.service.cuidado.MotorPlanoService;
import br.com.fiap.petbuddies.service.cadastro.ResponsavelService;
import br.com.fiap.petbuddies.web.form.NovoPlanoForm;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

// Cadastro de pacientes e, na ficha, o fluxo de instanciar plano de cuidado a partir de protocolo.
@Controller
@RequestMapping("/pacientes")
public class PacientesWebController {

    private final AnimalService animalService;
    private final ResponsavelService responsavelService;
    private final ConsultaService consultaService;
    private final MotorPlanoService motorPlanoService;

    public PacientesWebController(
            AnimalService animalService,
            ResponsavelService responsavelService,
            ConsultaService consultaService,
            MotorPlanoService motorPlanoService) {
        this.animalService = animalService;
        this.responsavelService = responsavelService;
        this.consultaService = consultaService;
        this.motorPlanoService = motorPlanoService;
    }

    // Os enums entram pelo Model, e nao por T(FQN) no template: assim o compilador
    // enxerga a referencia e um rename de pacote nao passa despercebido.
    @ModelAttribute("especies")
    public Especie[] especies() {
        return Especie.values();
    }

    @ModelAttribute("portes")
    public Porte[] portes() {
        return Porte.values();
    }

    @ModelAttribute("sexos")
    public Sexo[] sexos() {
        return Sexo.values();
    }

    @ModelAttribute("categoriasProtocolo")
    public CategoriaProtocolo[] categoriasProtocolo() {
        return CategoriaProtocolo.values();
    }

    @GetMapping
    public String listar(Model model) {
        List<AnimalResponse> pacientes = animalService.listar(null, null).stream()
                .map(AnimalResponse::from)
                .toList();
        Map<Long, String> nomesResponsaveis = responsavelService.listar(null).stream()
                .collect(Collectors.toMap(ResponsavelEntity::getId, ResponsavelEntity::getNome));

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("nomesResponsaveis", nomesResponsaveis);
        return "pacientes/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("animalRequest")) {
            model.addAttribute("animalRequest", new AnimalRequest());
        }
        model.addAttribute("responsaveis", listaResponsaveis());
        model.addAttribute("acaoFormulario", "/pacientes");
        return "pacientes/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("animalRequest") AnimalRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("responsaveis", listaResponsaveis());
            model.addAttribute("acaoFormulario", "/pacientes");
            return "pacientes/form";
        }
        AnimalEntity criado = animalService.criar(request);
        redirect.addFlashAttribute("sucesso", "Paciente \"" + criado.getNome() + "\" cadastrado.");
        return "redirect:/pacientes";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("animalRequest")) {
            model.addAttribute("animalRequest", AnimalRequest.from(animalService.buscarPorId(id)));
        }
        model.addAttribute("animalId", id);
        model.addAttribute("responsaveis", listaResponsaveis());
        model.addAttribute("acaoFormulario", "/pacientes/" + id);
        return "pacientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("animalRequest") AnimalRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("animalId", id);
            model.addAttribute("responsaveis", listaResponsaveis());
            model.addAttribute("acaoFormulario", "/pacientes/" + id);
            return "pacientes/form";
        }
        animalService.atualizar(id, request);
        redirect.addFlashAttribute("sucesso", "Paciente atualizado.");
        return "redirect:/pacientes/" + id;
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        AnimalEntity animal = animalService.buscarPorId(id);
        carregarFicha(animal, model);
        if (!model.containsAttribute("novoPlanoForm")) {
            model.addAttribute("novoPlanoForm", new NovoPlanoForm());
        }
        return "pacientes/ficha";
    }

    // Instancia plano de cuidado a partir do protocolo mais especifico.
    @PostMapping("/{id}/planos")
    public String instanciarPlano(
            @PathVariable Long id,
            @Valid @ModelAttribute("novoPlanoForm") NovoPlanoForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        AnimalEntity animal = animalService.buscarPorId(id);
        if (result.hasErrors()) {
            carregarFicha(animal, model);
            return "pacientes/ficha";
        }

        PlanoResponse resposta = form.getCategoria() == CategoriaProtocolo.POS_CIRURGICO
                ? motorPlanoService.instanciarPosCirurgico(
                        PlanoPosCirurgicoRequest.from(animal, consultaService.buscarPorId(form.getConsultaId())))
                : motorPlanoService.instanciarPreventivo(PlanoPreventivoRequest.from(animal));

        redirect.addFlashAttribute("planoResposta", resposta);
        return "redirect:/pacientes/" + id;
    }

    private List<ResponsavelResponse> listaResponsaveis() {
        return responsavelService.listar(null).stream().map(ResponsavelResponse::from).toList();
    }

    private void carregarFicha(AnimalEntity animal, Model model) {
        AnimalResponse animalResponse = AnimalResponse.from(animal);
        ResponsavelEntity responsavel = responsavelService.buscarPorId(animalResponse.getResponsavelId());
        List<ConsultaResponse> consultas = consultaService.listar(animal.getId(), null).stream()
                .map(ConsultaResponse::from)
                .toList();

        model.addAttribute("animal", animalResponse);
        model.addAttribute("responsavel", ResponsavelResponse.from(responsavel));
        model.addAttribute("consultas", consultas);
        motorPlanoService.buscarPlanoAtivo(animal.getId()).ifPresent(p -> model.addAttribute("planoAtivo", p));
    }

}
