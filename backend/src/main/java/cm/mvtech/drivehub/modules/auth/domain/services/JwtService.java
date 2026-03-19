package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
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
        return extractClaim(token, claims -> claims.get("tenant", String.class));
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
     * Génère un token pour un utilisateur normal (avec tenant)
     * Le tenant DOIT être passé en paramètre, pas depuis le TenantContext
     */
    public String generateToken(User user, String tenantId) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRoles().name());
        claims.put("profileStatus", user.getProfileStatus().name());
        claims.put("fullProfile", user.getFullProfile());
        claims.put("tokenType", "USER");
        claims.put("tenant", tenantId); // Passé explicitement

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Version avec TenantContext (pour compatibilité)
     */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRoles().name());
        claims.put("profileStatus", user.getProfileStatus().name());
        claims.put("fullProfile", user.getFullProfile());

        // CLÉ MULTITENANT
        claims.put("tenant", TenantContext.getTenantId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * JWT pour les admins de la plateforme (SANS tenant)
     */
    public String generatePlatformAdminToken(PlatformAdmin admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", admin.getRole().name());
        claims.put("tokenType", "PLATFORM_ADMIN");
        // PAS de tenant pour les admins platform

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(admin.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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