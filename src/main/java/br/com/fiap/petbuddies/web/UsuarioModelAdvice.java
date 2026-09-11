package br.com.fiap.petbuddies.web;

import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Põe o usuário logado e a seção atual em todo {@code Model} do pacote web. */
@ControllerAdvice(basePackages = "br.com.fiap.petbuddies.web")
public class UsuarioModelAdvice {

    @ModelAttribute("usuario")
    public UsuarioPrincipal usuario(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return principal;
    }

    /**
     * Primeiro segmento do caminho, que o menu compara para marcar o item atual.
     * Derivar aqui evita que cada controller tenha de lembrar de informar a seção —
     * e um item deixar de acender por esquecimento não daria erro nenhum.
     */
    @ModelAttribute("secao")
    public String secao(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        int inicio = caminho.startsWith("/") ? 1 : 0;
        int fim = caminho.indexOf('/', inicio);
        return fim < 0 ? caminho.substring(inicio) : caminho.substring(inicio, fim);
    }
}
