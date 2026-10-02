package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Génération et lecture des JWT.
 *
 * <p>Claims d'un utilisateur métier : role, profileStatus, fullProfile, tokenType=USER et
 * {@code tenant} = schéma de son auto-école (absent s'il n'en a pas encore).
 * Le tenant est calculé par le serveur ({@link UserTenantResolver}), jamais fourni par le client.</p>
 */
@Service
public class JwtService {

    public static final String CLAIM_TENANT = "tenant";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_USER = "USER";
    public static final String TOKEN_TYPE_PLATFORM_ADMIN = "PLATFORM_ADMIN";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-minutes:1440}")
    private long expirationMinutes;

    private Key signingKey;

    /** Vérifie la clé au démarrage plutôt qu'au premier login (HS256 exige 32 octets minimum). */
    @PostConstruct
    void init() {
        byte[] bytes = secretKey == null ? new byte[0] : secretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET_KEY) doit contenir au moins 32 caractères.");
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)   // vérifie aussi la signature ET l'expiration
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /* =========================
       EXTRACTION
       ========================= */

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTenant(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TENANT, String.class));
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            return true; // Si erreur de parsing, considérer comme expiré
        }
    }

    /* =========================
       GÉNÉRATION
       ========================= */

    /**
     * Token d'un utilisateur métier.
     *
     * @param tenant schéma de son auto-école, ou {@code null} s'il n'est rattaché à aucune
     */
    public String generateToken(User user, String tenant) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles().name());
        claims.put("profileStatus", user.getProfileStatus().name());
        claims.put("fullProfile", user.getFullProfile());
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_USER);
        if (tenant != null) {
            claims.put(CLAIM_TENANT, tenant);
        }
        return build(claims, user.getEmail());
    }

    /**
     * JWT pour les admins de la plateforme (SANS tenant)
     */
    public String generatePlatformAdminToken(PlatformAdmin admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", admin.getRole().name());
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_PLATFORM_ADMIN);
        return build(claims, admin.getEmail());
    }

    private String build(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMinutes * 60_000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validation du token pour un utilisateur normal
     */
    public boolean isTokenValid(String token, User user) {
        try {
            return extractEmail(token).equals(user.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validation du token pour un admin platform
     */
    public boolean isTokenValid(String token, String email) {
        try {
            return extractEmail(token).equals(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
