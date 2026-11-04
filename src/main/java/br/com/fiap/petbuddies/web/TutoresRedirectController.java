package br.com.fiap.petbuddies.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** O endereço antigo "tutores" segue valendo e leva para "responsáveis". */
@Controller
@RequestMapping("/tutores")
public class TutoresRedirectController {

    @GetMapping({"", "/novo", "/{id}/editar"})
    public String redirecionar(HttpServletRequest request) {
        return "redirect:/responsaveis" + request.getRequestURI().substring("/tutores".length());
    }
}
