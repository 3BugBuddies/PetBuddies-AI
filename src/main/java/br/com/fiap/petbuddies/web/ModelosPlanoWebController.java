package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.dto.modelo.ModeloPlanoResponse;
import br.com.fiap.petbuddies.service.cuidado.ModeloPlanoService;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Modelos de plano do catálogo (.NET), só leitura. Catálogo fora do ar vira aviso na tela, nunca erro. */
@Controller
@RequestMapping("/modelos-plano")
public class ModelosPlanoWebController {

    private final ModeloPlanoService modeloPlanoService;

    public ModelosPlanoWebController(ModeloPlanoService modeloPlanoService) {
        this.modeloPlanoService = modeloPlanoService;
    }

    @GetMapping
    public String listar(Model model) {
        var porEspecie = modeloPlanoService.listarPorEspecie();
        model.addAttribute("disponivel", porEspecie.isPresent());
        model.addAttribute("porEspecie", porEspecie.orElse(null));
        return "modelos-plano/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, @RequestParam(required = false) Long animalId, Model model) {
        Optional<ModeloPlanoResponse> modelo = modeloPlanoService.detalhar(id, animalId);
        model.addAttribute("disponivel", modelo.isPresent());
        model.addAttribute("modelo", modelo.orElse(null));
        return "modelos-plano/detalhe";
    }
}
