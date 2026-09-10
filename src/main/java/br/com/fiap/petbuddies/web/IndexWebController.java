package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Raiz autenticada: manda cada perfil para a sua própria página inicial. */
@Controller
public class IndexWebController {

    @GetMapping("/")
    public String raiz(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        if (usuario == null) {
            return "redirect:/login";
        }
        return usuario.isVet() ? "redirect:/painel" : "redirect:/meus-animais";
    }
}
