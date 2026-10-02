package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.RevokedToken;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.RevokedTokenRepository;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.support.TestData;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link TokenRevocationService} (déconnexion côté serveur).
 *
 * <p>On ne stocke jamais le jeton lui-même, seulement son empreinte SHA-256 (64 caractères
 * hexadécimaux) : une fuite de la table ne permettrait pas de réutiliser les jetons.</p>
 */
@ExtendWith(MockitoExtension.class)
class TokenRevocationServiceTest {

    @Mock private RevokedTokenRepository revokedTokenRepository;

    private JwtService jwtService;
    private TokenRevocationService service;
    private String token;

    @BeforeEach
    void setUp() {
        jwtService = JwtServiceTest.newJwtService("cle-secrete-de-test-jwt-32-caracteres-minimum!", 60);
        service = new TokenRevocationService(revokedTokenRepository, jwtService);
        token = jwtService.generateToken(TestData.user(Role.STUDENT), null);
    }

    /** Empreinte attendue, calculée indépendamment du service. */
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    /** revoke() enregistre l'empreinte SHA-256 du jeton et sa date d'expiration. */
    @Test
    void revoke_ShouldStoreSha256HashAndExpiration() throws Exception {
        when(revokedTokenRepository.existsById(sha256(token))).thenReturn(false);

        service.revoke(token);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        RevokedToken saved = captor.getValue();
        assertEquals(sha256(token), saved.getTokenHash());
        assertEquals(64, saved.getTokenHash().length());
        assertNotEquals(token, saved.getTokenHash(), "le jeton en clair ne doit jamais être stocké");

        LocalDateTime expectedExpiry = jwtService.extractClaim(token, Claims::getExpiration)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expectedExpiry, saved.getExpiresAt());
        assertTrue(ChronoUnit.SECONDS.between(saved.getRevokedAt(), LocalDateTime.now()) < 5);
    }

    /** Jeton déjà révoqué (double clic sur « Déconnexion ») : rien n'est réécrit. */
    @Test
    void revoke_AlreadyRevoked_ShouldDoNothing() throws Exception {
        when(revokedTokenRepository.existsById(sha256(token))).thenReturn(true);

        service.revoke(token);

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void isRevoked_ShouldLookUpTheHash() throws Exception {
        when(revokedTokenRepository.existsById(sha256(token))).thenReturn(true);
        when(revokedTokenRepository.existsById(sha256(token + "x"))).thenReturn(false);

        assertTrue(service.isRevoked(token));
        assertFalse(service.isRevoked(token + "x"));   // autre jeton -> autre empreinte -> non révoqué
    }

    /** Le nettoyage nocturne supprime les jetons dont l'expiration est passée. */
    @Test
    void purgeExpired_ShouldDeleteTokensExpiredBeforeNow() {
        when(revokedTokenRepository.deleteExpired(any(LocalDateTime.class))).thenReturn(3);

        LocalDateTime before = LocalDateTime.now();
        service.purgeExpired();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(revokedTokenRepository).deleteExpired(captor.capture());
        assertFalse(captor.getValue().isBefore(before));
        assertFalse(captor.getValue().isAfter(LocalDateTime.now()));
    }
}
