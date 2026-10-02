package cm.mvtech.drivehub.core.domain.service;

import cm.mvtech.drivehub.core.domain.entities.TenantEntity;
import cm.mvtech.drivehub.core.infrastructure.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link TenantProvisioningService}, limités à ce qui ne demande PAS de base
 * de données : la création réelle d'un schéma (Flyway) est couverte par le test d'intégration
 * {@code BusinessRoutesIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class TenantProvisioningServiceTest {

    @Mock private DataSource dataSource;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks
    private TenantProvisioningService service;

    /** Nom de schéma invalide : refus immédiat, aucune connexion ni écriture dans public.tenants. */
    @Test
    void createTenantSchema_WithInvalidName_ShouldBeRefused() {
        assertThrows(IllegalArgumentException.class, () -> service.createTenantSchema("x;DROP SCHEMA public"));
        assertThrows(IllegalArgumentException.class, () -> service.createTenantSchema("Majuscules"));
        assertThrows(IllegalArgumentException.class, () -> service.createTenantSchema(null));

        verifyNoInteractions(dataSource, tenantRepository);
    }

    /** Suspension d'une auto-école : le tenant est désactivé (les requêtes seront refusées). */
    @Test
    void setActive_ExistingTenant_ShouldUpdateFlag() {
        TenantEntity tenant = new TenantEntity("ae_ecole_abc123");
        tenant.setActive(true);
        when(tenantRepository.findByCode("ae_ecole_abc123")).thenReturn(Optional.of(tenant));

        service.setActive("ae_ecole_abc123", false);

        assertFalse(tenant.isActive());
        verify(tenantRepository).save(tenant);
    }

    @Test
    void setActive_UnknownTenant_ShouldDoNothing() {
        when(tenantRepository.findByCode("ae_inconnu")).thenReturn(Optional.empty());

        service.setActive("ae_inconnu", true);

        verify(tenantRepository, never()).save(any());
    }
}
