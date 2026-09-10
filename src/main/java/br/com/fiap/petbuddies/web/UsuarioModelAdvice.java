package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Põe o usuário logado em todo {@code Model} do pacote web, para o menu variar por perfil. */
@ControllerAdvice(basePackages = "br.com.fiap.petbuddies.web")
public class UsuarioModelAdvice {

    @ModelAttribute("usuario")
    public UsuarioPrincipal usuario(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return principal;
    }
}
