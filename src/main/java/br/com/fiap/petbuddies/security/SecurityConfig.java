package br.com.fiap.petbuddies.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * As duas cadeias, e as tres zonas de acesso.
 *
 * <p><b>A ordem e o que decide tudo.</b> A cadeia da API vem primeira e casa so
 * com {@code /api/**}; a da web pega o resto. Invertidas, o formulario passa a
 * interceptar as chamadas do app e o login do mobile devolve HTML em vez de
 * JSON.</p>
 *
 * <table>
 *   <caption>As tres zonas</caption>
 *   <tr><th>Zona</th><th>Quem entra</th><th>Como</th></tr>
 *   <tr><td>aberta</td><td>qualquer um</td>
 *       <td>login, motor, Swagger, documentacao, saude, estaticos</td></tr>
 *   <tr><td>token</td><td>o app</td><td>Bearer em {@code /api/**}</td></tr>
 *   <tr><td>sessao</td><td>o navegador</td><td>formulario, cookie, logout</td></tr>
 * </table>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cadeia da API — stateless, sem formulario e sem sessao.
     *
     * <p>{@code /api/motor/**} fica <b>aberto</b>: e a zona que o gatilho do
     * .NET usa, e como as chamadas de la sao best-effort, fecha-lo por engano
     * nao da erro visivel — o plano preventivo apenas para de nascer.</p>
     *
     * <p>O ponto de entrada e explicito porque, sem ele, uma cadeia sem
     * formulario nem basic recusa com 403. O contrato do app pede 401 para
     * "sem token" e deixa o 403 para "token valido, papel errado".</p>
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http, TokenService tokenService) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(rota -> rota
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/motor/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erro -> erro
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new TokenAuthenticationFilter(tokenService),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Cadeia da web — tudo o que nao e {@code /api/**}: formulario, sessao e
     * logout. As paginas que ela protege nascem no {@code J4}; aqui existe a
     * separacao que elas vao usar.
     *
     * <p><b>{@code /api-docs} nao casa com {@code /api/**}</b> — falta a barra.
     * Ele cai nesta cadeia, e sem liberacao explicita o Swagger fica atras do
     * formulario. Vale tambem para {@code /swagger-ui.html}, que responde com
     * um redirecionamento para {@code /swagger-ui/index.html}: sem liberar o
     * caminho com {@code .html}, o redirecionamento anonimo vai para o
     * formulario.</p>
     *
     * <p>O CSRF fica <b>ligado</b>: aqui ha cookie de sessao, e o formulario
     * gerado ja envia o token.</p>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(rota -> rota
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/api-docs", "/api-docs/**").permitAll()
                        .requestMatchers("/clinica/**").hasRole("VET")
                        .anyRequest().authenticated())
                .formLogin(formulario -> formulario.permitAll())
                .logout(saida -> saida.permitAll())
                .build();
    }

    /**
     * BCrypt. A coluna {@code DS_SENHA_HASH} tem 100 caracteres e o BCrypt gera
     * 60 — cabe com folga.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
