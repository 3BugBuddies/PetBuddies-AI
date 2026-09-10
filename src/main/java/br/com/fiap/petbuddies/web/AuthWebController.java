package br.com.fiap.petbuddies.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Só a página de login: o {@code POST} é tratado pelo filtro do Spring Security. */
@Controller
public class AuthWebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
