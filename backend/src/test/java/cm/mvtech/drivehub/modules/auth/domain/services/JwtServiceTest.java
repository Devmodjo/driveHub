package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.support.TestData;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de {@link JwtService}.
 *
 * <p>Le service est instancié « à la main » (sans Spring) : les valeurs normalement injectées
 * par {@code @Value} (clé secrète, durée de vie) sont posées avec {@link ReflectionTestUtils},
 * puis on appelle {@code init()} comme le ferait Spring ({@code @PostConstruct}).</p>
 */
class JwtServiceTest {

    private static final String SECRET = "cle-secrete-de-test-jwt-32-caracteres-minimum!";

    private JwtService jwtService;
    private User monitor;

    /** Crée un JwtService configuré avec la clé et la durée de vie données. */
    static JwtService newJwtService(String secret, long expirationMinutes) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "expirationMinutes", expirationMinutes);
        service.init();
        return service;
    }

    @BeforeEach
    void setUp() {
        jwtService = newJwtService(SECRET, 60);
        monitor = TestData.user(Role.MONITOR);
        monitor.setProfileStatus(ProfileStatus.ACTIVE);
    }

    // ─── jeton utilisateur ────────────────────────────────────────────────────

    /** Le jeton porte l'email (subject), le rôle, le type USER et le tenant calculé par le serveur. */
    @Test
    void generateToken_ShouldContainRoleTenantAndTokenType() {
        String token = jwtService.generateToken(monitor, "ae_ecole_abc123");

        assertEquals(monitor.getEmail(), jwtService.extractEmail(token));
        assertEquals("ae_ecole_abc123", jwtService.extractTenant(token));
        assertEquals("MONITOR", jwtService.extractClaim(token, c -> c.get("role", String.class)));
        assertEquals(JwtService.TOKEN_TYPE_USER,
                jwtService.extractClaim(token, c -> c.get(JwtService.CLAIM_TOKEN_TYPE, String.class)));
        assertEquals("ACTIVE", jwtService.extractClaim(token, c -> c.get("profileStatus", String.class)));
    }

    /** Utilisateur sans auto-école : pas de claim tenant (et surtout pas « public »). */
    @Test
    void generateToken_WithoutTenant_ShouldNotContainTenantClaim() {
        String token = jwtService.generateToken(monitor, null);

        assertNull(jwtService.extractTenant(token));
        Boolean hasTenantClaim = jwtService.extractClaim(token, c -> c.containsKey(JwtService.CLAIM_TENANT));
        assertFalse(hasTenantClaim);
    }

    /** L'expiration vaut « maintenant + expiration-minutes » (à quelques secondes près). */
    @Test
    void generateToken_ShouldExpireAfterConfiguredDuration() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken(monitor, null);

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals(60 * 60_000L, expiration.getTime() - issuedAt.getTime());
        assertTrue(expiration.getTime() >= before + 59 * 60_000L);
        assertFalse(jwtService.isTokenExpired(token));
    }

    // ─── jeton administrateur plateforme ──────────────────────────────────────

    @Test
    void generatePlatformAdminToken_ShouldHaveAdminTypeAndNoTenant() {
        PlatformAdmin admin = new PlatformAdmin();
        admin.setEmail("root@drivehub.cm");
        admin.setRole(AdminRole.ROOT);

        String token = jwtService.generatePlatformAdminToken(admin);

        assertEquals("root@drivehub.cm", jwtService.extractEmail(token));
        assertEquals("ROOT", jwtService.extractClaim(token, c -> c.get("role", String.class)));
        assertEquals(JwtService.TOKEN_TYPE_PLATFORM_ADMIN,
                jwtService.extractClaim(token, c -> c.get(JwtService.CLAIM_TOKEN_TYPE, String.class)));
        assertNull(jwtService.extractTenant(token));
        assertTrue(jwtService.isTokenValid(token, "root@drivehub.cm"));
    }

    // ─── validation ───────────────────────────────────────────────────────────

    @Test
    void isTokenValid_ShouldCheckOwner() {
        String token = jwtService.generateToken(monitor, null);

        assertTrue(jwtService.isTokenValid(token, monitor));
        assertFalse(jwtService.isTokenValid(token, TestData.user(Role.MONITOR)));
        assertFalse(jwtService.isTokenValid(token, "autre@test.cm"));
    }

    /** Durée de vie négative : le jeton est déjà expiré à sa création. */
    @Test
    void expiredToken_ShouldBeRejected() {
        JwtService shortLived = newJwtService(SECRET, -1);
        String token = shortLived.generateToken(monitor, null);

        assertTrue(shortLived.isTokenExpired(token));
        assertFalse(shortLived.isTokenValid(token, monitor));
    }

    /** Un jeton signé avec une autre clé (forgé) n'est jamais accepté. */
    @Test
    void tokenSignedWithAnotherKey_ShouldBeRejected() {
        String forged = newJwtService("une-toute-autre-cle-secrete-de-32-caracteres", 60)
                .generateToken(monitor, "ae_ecole_abc123");

        assertFalse(jwtService.isTokenValid(forged, monitor));
        assertTrue(jwtService.isTokenExpired(forged), "un jeton illisible est traité comme expiré");
        assertThrows(Exception.class, () -> jwtService.extractTenant(forged));
    }

    /** Clé trop courte pour HS256 : l'application refuse de démarrer. */
    @Test
    void init_WithTooShortSecret_ShouldFail() {
        assertThrows(IllegalStateException.class, () -> newJwtService("trop-courte", 60));
        assertThrows(IllegalStateException.class, () -> newJwtService(null, 60));
    }
}
