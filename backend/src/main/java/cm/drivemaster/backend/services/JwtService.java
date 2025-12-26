package cm.drivemaster.backend.services;

import cm.drivemaster.backend.beans.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.function.Function;

@Service
public class JwtService {

    /**
     * ⚠️ IMPORTANT
     * - Mets cette clé dans application.yml en PROD
     * - Longueur MINIMUM recommandée : 256 bits
     */
    private static final String SECRET_KEY =
            "3f8a9b2c7d4e1f6a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2";

    /**
     * Durée de validité du token : 24h
     */
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    /* =========================
       MÉTHODES INTERNES
       ========================= */

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

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /* =========================
        EXTRACTION DES DONNÉES
       ========================= */

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /* =========================
        GÉNÉRATION DU TOKEN
       ========================= */

    /**
     * Génère un JWT pour un utilisateur
     * Le tenant est DÉJÀ positionné par TenantResolutionFilter
     */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        // Rôle principal
        claims.put("role", user.getRoles().name());

        // État du profil (utile côté frontend)
        claims.put("profileStatus", user.getProfileStatus().name());
        claims.put("fullProfile", user.getFullProfile());

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

    /* =========================
       ✅ VALIDATION DU TOKEN
       ========================= */

    public boolean isTokenValid(String token, User user) {
        return extractEmail(token).equals(user.getEmail())
                && !isTokenExpired(token);
    }
}
