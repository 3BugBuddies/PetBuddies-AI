package br.com.fiap.petbuddies.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Le {@code Authorization: Bearer}, valida o token e popula o contexto com o
 * perfil como papel.
 *
 * <p><b>Nao e um bean.</b> Todo bean do tipo {@code Filter} e registrado pelo
 * Spring Boot no container de servlets e passa a rodar em <i>toda</i>
 * requisicao, inclusive nas da cadeia web. Ele e construido a mao no
 * {@code SecurityConfig} e entra so na cadeia da API.</p>
 *
 * <p><b>Nunca lanca.</b> Token ausente, malformado ou expirado apenas deixa o
 * contexto vazio, e quem decide o desfecho e o ponto de entrada da cadeia da
 * API, com 401. Lancar aqui trocaria esse 401 por um 500 causado por um
 * cabecalho velho.</p>
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null
                && cabecalho.startsWith(PREFIXO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(cabecalho.substring(PREFIXO.length()).trim(), request);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        try {
            Claims claims = tokenService.validar(token);
            String perfil = claims.get(TokenService.CLAIM_PERFIL, String.class);
            if (perfil == null || perfil.isBlank()) {
                log.debug("Token sem a claim de perfil; requisicao segue anonima.");
                return;
            }
            var autenticacao = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
            autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        } catch (JwtException | IllegalArgumentException e) {
            // Sem o token no log, e sem mensagem ao cliente: quem responde e o
            // ponto de entrada da cadeia, com 401 e corpo vazio.
            log.debug("Token recusado: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
