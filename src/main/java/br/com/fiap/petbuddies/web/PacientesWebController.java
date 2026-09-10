package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.dto.AnimalRequest;
import br.com.fiap.petbuddies.dto.AnimalResponse;
import br.com.fiap.petbuddies.dto.ConsultaResponse;
import br.com.fiap.petbuddies.dto.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.PlanoResponse;
import br.com.fiap.petbuddies.dto.ResponsavelResponse;
import br.com.fiap.petbuddies.service.AnimalService;
import br.com.fiap.petbuddies.service.ConsultaService;
import br.com.fiap.petbuddies.service.MotorPlanoService;
import br.com.fiap.petbuddies.service.ResponsavelService;
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

/**
 * Cadastro de pacientes (item 1) e, na ficha, o fluxo 1 da rubrica —
 * instanciar plano de cuidado a partir de protocolo (item 4).
 */
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
            model.addAttribute("animalRequest", paraRequest(animalService.buscarPorId(id)));
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

    /** Fluxo 1: instanciar plano de cuidado a partir do protocolo mais específico. */
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
                ? motorPlanoService.instanciarPosCirurgico(paraRequestPosCirurgico(animal, form.getConsultaId()))
                : motorPlanoService.instanciarPreventivo(paraRequestPreventivo(animal));

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

    private AnimalRequest paraRequest(AnimalEntity animal) {
        AnimalRequest request = new AnimalRequest();
        request.setNome(animal.getNome());
        request.setEspecie(animal.getEspecie());
        request.setRaca(animal.getRaca());
        request.setPorte(animal.getPorte());
        request.setSexo(animal.getSexo());
        request.setDataNascimento(animal.getDataNascimento());
        request.setPeso(animal.getPeso());
        request.setCondicaoCronica(animal.isCondicaoCronica());
        request.setCastrado(animal.isCastrado());
        request.setFoto(animal.getFoto());
        request.setAlergias(animal.getAlergias());
        request.setObservacoes(animal.getObservacoes());
        request.setResponsavelId(animal.getResponsavel().getId());
        return request;
    }

    private PlanoPreventivoRequest paraRequestPreventivo(AnimalEntity animal) {
        PlanoPreventivoRequest request = new PlanoPreventivoRequest();
        request.setAnimalId(animal.getId());
        request.setEspecie(animal.getEspecie());
        request.setDataNascimento(animal.getDataNascimento());
        return request;
    }

    private PlanoPosCirurgicoRequest paraRequestPosCirurgico(AnimalEntity animal, Long consultaId) {
        ConsultaEntity consulta = consultaService.buscarPorId(consultaId);
        PlanoPosCirurgicoRequest request = new PlanoPosCirurgicoRequest();
        request.setAnimalId(animal.getId());
        request.setConsultaId(consultaId);
        request.setEspecie(animal.getEspecie());
        request.setDataRealizacao(consulta.getDataHora());
        return request;
    }
}
