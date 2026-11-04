package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Raiz autenticada: a veterinária vai para o início do painel; o responsável, para o aviso do app. */
@Controller
public class IndexWebController {

    @GetMapping("/")
    public String raiz(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        if (usuario == null) {
            return "redirect:/login";
        }
        return usuario.isVet() ? "redirect:/painel" : "redirect:/acesso-app";
    }

    @GetMapping("/acesso-app")
    public String acessoApp(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return usuario != null && usuario.isVet() ? "redirect:/painel" : "somente-app";
    }
}
