package br.com.fiap.petbuddies.handler;

import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.CnpjDuplicadoException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaJaRealizadaException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoPodeSerFechadaException;
import br.com.fiap.petbuddies.exception.atendimento.CodigoCondicaoDuplicadoException;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.exception.cadastro.CrmvDuplicadoException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaAtendimentoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.cuidado.PlanoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.prescricao.PrescricaoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.ProcedimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoIncoerenteException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import br.com.fiap.petbuddies.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Segundo interceptador de exceção, restrito ao pacote {@code web} (J4).
 *
 * <p>{@link GlobalExceptionHandler} é {@code @RestControllerAdvice} sem
 * restrição de pacote — sem este advice, ordenado à frente dele, um erro de
 * domínio dentro de uma tela voltaria como JSON, não como página.</p>
 */
@ControllerAdvice(basePackages = "br.com.fiap.petbuddies.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler({
            AnimalNaoEncontradoException.class,
            ClinicaNaoEncontradaException.class,
            CondicaoClinicaNaoEncontradaException.class,
            ConsultaNaoEncontradaException.class,
            JanelaAtendimentoNaoEncontradaException.class,
            PlanoNaoEncontradoException.class,
            PrescricaoNaoEncontradaException.class,
            ProcedimentoNaoEncontradoException.class,
            RegistroAtendimentoNaoEncontradoException.class,
            RegraPrescricaoNaoEncontradaException.class,
            ResponsavelNaoEncontradoException.class,
            VeterinarioNaoEncontradoException.class
    })
    public ModelAndView handleNaoEncontrado(RuntimeException ex, HttpServletResponse response) {
        return erro(response, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            CnpjDuplicadoException.class,
            CrmvDuplicadoException.class,
            CodigoCondicaoDuplicadoException.class,
            JanelaConflitanteException.class,
            ConsultaJaRealizadaException.class,
            ConsultaNaoPodeSerFechadaException.class
    })
    public ModelAndView handleConflito(RuntimeException ex, HttpServletResponse response) {
        return erro(response, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RegraPrescricaoIncoerenteException.class)
    public ModelAndView handleIncoerente(RegraPrescricaoIncoerenteException ex, HttpServletResponse response) {
        return erro(response, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ModelAndView handleCredenciaisInvalidas(CredenciaisInvalidasException ex, HttpServletResponse response) {
        return erro(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenerico(Exception ex, HttpServletResponse response) {
        log.error("Erro não tratado numa tela web", ex);
        return erro(response, HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }

    private ModelAndView erro(HttpServletResponse response, HttpStatus status, String mensagem) {
        response.setStatus(status.value());
        ModelAndView mv = new ModelAndView("erro");
        mv.addObject("mensagem", mensagem);
        // O ModelAndView é novo aqui — não herda o Model original, então o menu
        // do layout perderia o usuário logado sem repor este atributo.
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (principal instanceof UsuarioPrincipal usuario) {
            mv.addObject("usuario", usuario);
        }
        return mv;
    }
}
