package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.RevokedToken;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

/**
 * Déconnexion côté serveur (logout).
 *
 * <p>Un JWT est « sans état » : le serveur ne garde pas de session, donc un jeton reste valide
 * jusqu'à son expiration. Pour qu'un logout soit réel (et pas seulement « oublier le jeton » dans
 * le navigateur), on inscrit le jeton dans une liste noire :</p>
 * <ol>
 *   <li>{@link #revoke(String)} : appelé par {@code POST /api/auth/logout} et
 *       {@code POST /api/platform/admin/logout} ;</li>
 *   <li>{@link #isRevoked(String)} : appelé par {@code JwtAuthenticationFilter} à chaque requête ;
 *       un jeton révoqué est traité comme absent (réponse 401 sur les routes protégées) ;</li>
 *   <li>{@link #purgeExpired()} : nettoyage automatique chaque nuit.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    /** Invalide le jeton jusqu'à son expiration. Sans effet s'il est déjà révoqué. */
    @Transactional
    public void revoke(String token) {
        String hash = sha256(token);
        if (revokedTokenRepository.existsById(hash)) {
            return;
        }
        LocalDateTime expiresAt = jwtService.extractClaim(token, Claims::getExpiration)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        revokedTokenRepository.save(new RevokedToken(hash, expiresAt, LocalDateTime.now()));
    }

    /** {@code true} si l'utilisateur s'est déconnecté avec ce jeton. */
    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsById(sha256(token));
    }

    /** Chaque nuit à 3 h : suppression des jetons révoqués qui ont expiré entre-temps. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        int deleted = revokedTokenRepository.deleteExpired(LocalDateTime.now());
        log.info("{} jeton(s) révoqué(s) expiré(s) supprimé(s)", deleted);
    }

    /** Empreinte SHA-256 du jeton, en hexadécimal (64 caractères). */
    private static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);   // n'arrive pas sur une JVM standard
        }
    }
}
