package br.com.fiap.petbuddies.security;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
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
 * Emite e valida o token do ADR {@code s3-20}. <b>Nada aqui e escolha deste
 * servico:</b> o .NET valida o mesmo token com o mesmo segredo, e qualquer
 * divergencia de emissor, claim ou algoritmo aparece do outro lado como um 401
 * generico que nao diz qual campo mudou.
 *
 * <p>Formato fixado: HS256, emissor {@code petbuddies-ai}, sujeito com o id do
 * usuario como texto, e as claims {@code perfil}, {@code usuarioId} e
 * <b>exatamente um</b> de {@code veterinarioId} / {@code responsavelId} — o
 * vinculo que nao existe fica ausente, nao nulo. Validade de oito horas, sem
 * refresh.</p>
 *
 * <p>A chave e montada no construtor de proposito: com segredo abaixo de 32
 * bytes a biblioteca recusa a chave e a aplicacao nao sobe, em vez de emitir
 * token fraco em silencio.</p>
 */
@Service
public class TokenService {

    /** Emissor do ADR s3-20. O .NET exige este valor exato. */
    public static final String EMISSOR = "petbuddies-ai";

    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_USUARIO_ID = "usuarioId";
    public static final String CLAIM_VETERINARIO_ID = "veterinarioId";
    public static final String CLAIM_RESPONSAVEL_ID = "responsavelId";

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

    /** Emite o token de um usuario ja autenticado. */
    public String emitir(UsuarioEntity usuario) {
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
        return builder.signWith(chave).compact();
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
