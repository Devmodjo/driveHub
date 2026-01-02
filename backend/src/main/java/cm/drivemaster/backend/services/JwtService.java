package cm.drivemaster.backend.services;

import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.core.TenantContext;
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

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

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
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }

    /* =========================
       GÉNÉRATION
       ========================= */

    /**
     * Le tenant est positionné dans TenantContext
     */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRoles().name());
        claims.put("profileStatus", user.getProfileStatus().name());
        claims.put("fullProfile", user.getFullProfile());
        claims.put("tokenType", "USER");

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
     * JWT pour les admin de la platform
     * @param admin object
     * @return jwt token
     */
    public String generatePlatformAdminToken(PlatformAdmin admin) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", admin.getRole().name());
        claims.put("tokenType", "PLATFORM_ADMIN");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(admin.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean isTokenValid(String token, User user) {
        return extractEmail(token).equals(user.getEmail())
                && !isTokenExpired(token);
    }
}
