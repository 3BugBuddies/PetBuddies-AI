package br.com.fiap.petbuddies.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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

// A ordem importa: a cadeia da API vem primeiro e casa so com /api/**; invertida, o formulario intercepta as chamadas do app.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * As rotas cujo POST, PUT e DELETE sao ato clinico. O rascunho de prescricao
     * nao precisa entrar: /api/prescricao ja cobre ele por prefixo.
     */
    private static final String[] ESCRITAS_CLINICAS = {
            "/api/consulta/**", "/api/consulta",
            "/api/registro-atendimento/**", "/api/registro-atendimento",
            "/api/procedimento/**", "/api/procedimento",
            "/api/prescricao/**", "/api/prescricao",
            "/api/regra-prescricao/**", "/api/regra-prescricao",
            "/api/condicao-clinica/**", "/api/condicao-clinica",
            "/api/janela-atendimento/**", "/api/janela-atendimento",
            "/api/motor/plano/**",
            "/api/animal/**", "/api/animal",
            "/api/responsavel/**", "/api/responsavel",
            "/api/veterinario/**", "/api/veterinario",
            "/api/clinica/**", "/api/clinica"
    };

    // Ponto de entrada explicito: sem ele, cadeia sem formulario nem basic recusa com 403 em vez de 401.
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
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()

                        // O relato do dia e do tutor, e so dele: quem escreve check-in
                        // e quem convive com o animal.
                        .requestMatchers(HttpMethod.POST, "/api/checkin", "/api/checkin/extracao").hasRole("TUTOR")

                        // Precisa vir antes da regra geral de DELETE; o dono do animal e conferido no AnimalService.
                        .requestMatchers(HttpMethod.DELETE, "/api/animal/**").hasAnyRole("VET", "TUTOR")

                        // Ato clinico: o TUTOR nao autora, nao agenda e nao fecha atendimento.
                        // A regra e por metodo, nao por rota — os GET seguem abertos a
                        // qualquer token, porque o tutor precisa ler o proprio animal.
                        .requestMatchers(HttpMethod.POST, ESCRITAS_CLINICAS).hasRole("VET")
                        .requestMatchers(HttpMethod.PUT, ESCRITAS_CLINICAS).hasRole("VET")
                        .requestMatchers(HttpMethod.DELETE, ESCRITAS_CLINICAS).hasRole("VET")

                        .anyRequest().authenticated())
                .exceptionHandling(erro -> erro
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new TokenAuthenticationFilter(tokenService),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Tudo o que nao e /api/**: formulario, sessao e logout.
    // /api-docs e /swagger-ui.html nao casam com /api/** — sem liberacao explicita aqui, o Swagger cai atras do formulario.
    // CSRF fica ligado — ha cookie de sessao.
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(rota -> rota
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Sem isto o icone da aba cai no authenticated() e a tela de
                        // login pede o proprio login para desenhar o favicon.
                        .requestMatchers("/favicon.png", "/apple-touch-icon.png").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/api-docs", "/api-docs/**").permitAll()
                        .requestMatchers("/painel/**", "/clinica/**", "/equipe/**",
                                "/tutores/**", "/pacientes/**", "/agenda/**").hasRole("VET")
                        .requestMatchers("/meus-animais", "/meus-animais/**").hasRole("TUTOR")
                        .anyRequest().authenticated())
                .formLogin(formulario -> formulario.loginPage("/login").permitAll())
                .logout(saida -> saida.permitAll())
                .build();
    }

    // BCrypt gera hash de 60 caracteres — cabe na coluna de 100.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
