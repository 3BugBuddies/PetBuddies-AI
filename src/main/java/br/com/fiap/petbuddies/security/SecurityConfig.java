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
                        // Ato clinico: perfil TUTOR nao autora prescricao.
                        .requestMatchers(HttpMethod.POST, "/api/prescricao/rascunho").hasRole("VET")
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
