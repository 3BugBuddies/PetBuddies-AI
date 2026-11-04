package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloRequest;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloResponse;
import br.com.fiap.petbuddies.exception.prescricao.ReceitaModeloIncoerenteException;
import br.com.fiap.petbuddies.exception.prescricao.TituloReceitaModeloDuplicadoException;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import br.com.fiap.petbuddies.service.prescricao.ReceitaModeloService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Receitas-modelo da clínica. A prescrição copia o conteúdo; editar aqui nunca muda receita assinada. */
@Controller
@RequestMapping("/receitas-modelo")
public class ReceitasModeloWebController {

    /** Opção de select: o símbolo só existe nos operadores e alimenta a prévia da regra. */
    public record Opcao(String valor, String rotulo, String simbolo) {}

    private final ReceitaModeloService service;

    public ReceitasModeloWebController(ReceitaModeloService service) {
        this.service = service;
    }

    @ModelAttribute("operadores")
    public List<Opcao> operadores() {
        return List.of(
                new Opcao(OperadorRegra.MAIOR_QUE.name(), "maior que", ">"),
                new Opcao(OperadorRegra.MAIOR_OU_IGUAL.name(), "maior ou igual a", "≥"),
                new Opcao(OperadorRegra.MENOR_QUE.name(), "menor que", "<"),
                new Opcao(OperadorRegra.MENOR_OU_IGUAL.name(), "menor ou igual a", "≤"),
                new Opcao(OperadorRegra.IGUAL.name(), "igual a", "="));
    }

    @ModelAttribute("acoes")
    public List<Opcao> acoes() {
        return List.of(
                new Opcao(TipoAcaoRegra.DOSE_MIN.name(), "dose mínima", null),
                new Opcao(TipoAcaoRegra.DOSE_MAX.name(), "dose máxima", null),
                new Opcao(TipoAcaoRegra.DOSE_PADRAO.name(), "dose padrão", null),
                new Opcao(TipoAcaoRegra.ACIONAR_CLINICA.name(), "acionar clínica", null),
                new Opcao(TipoAcaoRegra.SUSPENDER.name(), "suspender", null));
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        List<ReceitaModeloResponse> receitas = service.listar(usuario.getUsuarioId()).stream()
                .map(ReceitaModeloResponse::from)
                .toList();
        model.addAttribute("receitas", receitas);
        return "receitas-modelo/lista";
    }

    @GetMapping("/novo")
    public String novoForm(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        if (!model.containsAttribute("receitaModeloRequest")) {
            model.addAttribute("receitaModeloRequest", new ReceitaModeloRequest());
        }
        return formulario(usuario, model, null, "/receitas-modelo");
    }

    @PostMapping
    public String criar(
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("receitaModeloRequest") ReceitaModeloRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(usuario, model, null, "/receitas-modelo");
        }
        try {
            ReceitaModeloEntity criada = service.criar(request, usuario.getUsuarioId());
            redirect.addFlashAttribute("sucesso", "Receita-modelo \"" + criada.getTitulo() + "\" criada.");
            return "redirect:/receitas-modelo";
        } catch (ReceitaModeloIncoerenteException | TituloReceitaModeloDuplicadoException e) {
            model.addAttribute("erroDominio", e.getMessage());
            return formulario(usuario, model, null, "/receitas-modelo");
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        ReceitaModeloEntity receita = service.buscarPorId(id, usuario.getUsuarioId());
        if (!model.containsAttribute("receitaModeloRequest")) {
            model.addAttribute("receitaModeloRequest", ReceitaModeloRequest.from(receita));
        }
        model.addAttribute("versao", receita.getVersao());
        return formulario(usuario, model, id, "/receitas-modelo/" + id);
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario,
            @Valid @ModelAttribute("receitaModeloRequest") ReceitaModeloRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(usuario, model, id, "/receitas-modelo/" + id);
        }
        try {
            ReceitaModeloEntity atualizada = service.atualizar(id, request, usuario.getUsuarioId());
            redirect.addFlashAttribute("sucesso",
                    "Receita-modelo \"" + atualizada.getTitulo() + "\" salva na versão " + atualizada.getVersao() + ".");
            return "redirect:/receitas-modelo";
        } catch (ReceitaModeloIncoerenteException | TituloReceitaModeloDuplicadoException e) {
            model.addAttribute("erroDominio", e.getMessage());
            return formulario(usuario, model, id, "/receitas-modelo/" + id);
        }
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal usuario,
                            RedirectAttributes redirect) {
        service.desativar(id, usuario.getUsuarioId());
        redirect.addFlashAttribute("sucesso", "Receita-modelo desativada.");
        return "redirect:/receitas-modelo";
    }

    private String formulario(UsuarioPrincipal usuario, Model model, Long receitaId, String acao) {
        List<CondicaoClinicaResponse> condicoes = service.listarCondicoesAtivas(usuario.getUsuarioId()).stream()
                .map(CondicaoClinicaResponse::from)
                .toList();
        model.addAttribute("condicoes", condicoes);
        model.addAttribute("receitaId", receitaId);
        model.addAttribute("acaoFormulario", acao);
        return "receitas-modelo/editor";
    }
}
