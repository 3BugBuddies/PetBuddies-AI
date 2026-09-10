package br.com.fiap.petbuddies.security;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.enums.PerfilUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Formato fixado com o .NET, que valida este mesmo token: HS256, emissor
 * {@code petbuddies-ai}, sujeito com o id do usuario como texto, e as claims
 * {@code perfil}, {@code usuarioId}, <b>exatamente um</b> de
 * {@code veterinarioId} / {@code responsavelId}, e {@code clinicaId} so no
 * perfil VET. Claim que nao se aplica fica <b>ausente</b>, nunca nula.
 * Mudanca aqui aparece do outro lado como 401 generico.
 */
@Service
public class TokenService {

    /** Emissor do ADR s3-20. O .NET exige este valor exato. */
    public static final String EMISSOR = "petbuddies-ai";

    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_USUARIO_ID = "usuarioId";
    public static final String CLAIM_VETERINARIO_ID = "veterinarioId";
    public static final String CLAIM_RESPONSAVEL_ID = "responsavelId";
    public static final String CLAIM_CLINICA_ID = "clinicaId";

    /** Sujeito do token de servico — nao corresponde a nenhuma linha de T_PB_USUARIO. */
    private static final String SUJEITO_SERVICO = "motor-planos";

    /** Tolerancia de relogio combinada com o N4 (README da onda 2). */
    private static final long TOLERANCIA_RELOGIO_SEGUNDOS = 30L;

    private final SecretKey chave;
    private final long expiracaoHoras;

    public TokenService(
            @Value("${petbuddies.jwt.secret}") String segredo,
            @Value("${petbuddies.jwt.expiracao-horas}") long expiracaoHoras) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoHoras = expiracaoHoras;
    }

    /**
     * Emite o token de um usuario ja autenticado.
     *
     * @param clinicaId clinica do veterinario, ou {@code null} no perfil TUTOR.
     *                  Resolvido pelo chamador: este servico nao le repositorio.
     */
    public String emitir(UsuarioEntity usuario, Long clinicaId) {
        Instant agora = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .issuer(EMISSOR)
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .claim(CLAIM_USUARIO_ID, usuario.getId())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoHoras, ChronoUnit.HOURS)));

        // O check CK_USUARIO_VINCULO do banco garante que so um dos dois esta
        // preenchido; aqui a consequencia e so nao escrever a claim vazia.
        if (usuario.getVeterinarioId() != null) {
            builder.claim(CLAIM_VETERINARIO_ID, usuario.getVeterinarioId());
        }
        if (usuario.getResponsavelId() != null) {
            builder.claim(CLAIM_RESPONSAVEL_ID, usuario.getResponsavelId());
        }
        // Ausente no perfil TUTOR: responsavel nao pertence a clinica no schema.
        if (clinicaId != null) {
            builder.claim(CLAIM_CLINICA_ID, clinicaId);
        }
        return builder.signWith(chave).compact();
    }

    /**
     * Token de servico a servico, sem usuario por tras — quem chama e o motor de
     * planos lendo o catalogo do .NET (ADR s3-25), nao uma sessao de app.
     * Claim {@code perfil=VET} porque e o unico papel que o endpoint exige;
     * vida curta porque e emitido de novo a cada chamada, nunca guardado.
     */
    public String emitirServico() {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(EMISSOR)
                .subject(SUJEITO_SERVICO)
                .claim(CLAIM_PERFIL, PerfilUsuario.VET.name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(5, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    /**
     * Confere assinatura, emissor e expiracao, e devolve as claims.
     *
     * @throws JwtException token ausente de assinatura valida, expirado, ou
     *                      emitido por outro emissor
     */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(EMISSOR)
                .clockSkewSeconds(TOLERANCIA_RELOGIO_SEGUNDOS)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
