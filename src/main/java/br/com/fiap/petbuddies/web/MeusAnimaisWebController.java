package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.cuidado.PlanoResponse;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import br.com.fiap.petbuddies.service.cadastro.AnimalService;
import br.com.fiap.petbuddies.service.cuidado.MotorPlanoService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A única tela do tutor. O id do responsável vem do principal da sessão —
 * nunca de parâmetro de URL — para que um tutor só veja os próprios animais.
 */
@Controller
@RequestMapping("/meus-animais")
public class MeusAnimaisWebController {

    private final AnimalService animalService;
    private final MotorPlanoService motorPlanoService;

    public MeusAnimaisWebController(AnimalService animalService, MotorPlanoService motorPlanoService) {
        this.animalService = animalService;
        this.motorPlanoService = motorPlanoService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        Long responsavelId = usuario.getResponsavelId();
        List<AnimalResponse> animais = responsavelId == null
                ? List.of()
                : animalService.listar(responsavelId, null).stream().map(AnimalResponse::from).toList();

        Map<Long, PlanoResponse> planosPorAnimal = new LinkedHashMap<>();
        for (AnimalResponse animal : animais) {
            motorPlanoService.buscarPlanoAtivo(animal.getId()).ifPresent(p -> planosPorAnimal.put(animal.getId(), p));
        }

        model.addAttribute("animais", animais);
        model.addAttribute("planosPorAnimal", planosPorAnimal);
        return "meus-animais";
    }
}
