package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.dto.atendimento.PrevisaoJanelas;
import br.com.fiap.petbuddies.exception.atendimento.PeriodoInvalidoException;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import br.com.fiap.petbuddies.service.atendimento.JanelaAtendimentoService;
import br.com.fiap.petbuddies.service.cadastro.VeterinarioService;
import br.com.fiap.petbuddies.web.form.HorariosForm;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Horários da veterinária logada: um modelo semanal que abre as janelas, sem ficar gravado. */
@Controller
@RequestMapping("/horarios")
public class HorariosWebController {

    private final JanelaAtendimentoService janelaAtendimentoService;
    private final VeterinarioService veterinarioService;

    public HorariosWebController(JanelaAtendimentoService janelaAtendimentoService, VeterinarioService veterinarioService) {
        this.janelaAtendimentoService = janelaAtendimentoService;
        this.veterinarioService = veterinarioService;
    }

    // Linhas removidas deixam buracos nos índices; o binder precisa aceitar o maior índice que a tela mandar.
    @InitBinder("horariosForm")
    public void binder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1024);
    }

    @GetMapping
    public String formulario(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        if (!model.containsAttribute("horariosForm")) {
            model.addAttribute("horariosForm", HorariosForm.padrao());
        }
        return pagina(usuario, model);
    }

    @PostMapping("/previa")
    public String previa(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("horariosForm") HorariosForm form,
            BindingResult result,
            Model model) {
        if (!result.hasErrors()) {
            try {
                PrevisaoJanelas previsao = janelaAtendimentoService.prever(usuario.getVeterinarioId(), form.toModelo());
                model.addAttribute("previsao", previsao);
            } catch (PeriodoInvalidoException e) {
                model.addAttribute("erroModelo", e.getMessage());
            }
        }
        return pagina(usuario, model);
    }

    @PostMapping("/gerar")
    public String gerar(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("horariosForm") HorariosForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                int criadas = janelaAtendimentoService.gerar(usuario.getVeterinarioId(), form.toModelo());
                redirect.addFlashAttribute("horariosForm", form);
                redirect.addFlashAttribute("sucesso", criadas == 1
                        ? "1 janela aberta."
                        : criadas + " janelas abertas. As que já existiam ficaram como estavam.");
                return "redirect:/horarios";
            } catch (PeriodoInvalidoException e) {
                model.addAttribute("erroModelo", e.getMessage());
            }
        }
        return pagina(usuario, model);
    }

    private String pagina(UsuarioPrincipal usuario, Model model) {
        model.addAttribute("vet", veterinarioService.buscarPorId(usuario.getVeterinarioId()));
        model.addAttribute("diasDaSemana", DayOfWeek.values());
        return "horarios/modelo";
    }
}
