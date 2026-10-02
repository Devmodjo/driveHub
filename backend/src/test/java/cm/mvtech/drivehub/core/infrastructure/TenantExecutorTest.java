package cm.mvtech.drivehub.core.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link TenantExecutor}.
 *
 * <p>Le gestionnaire de transactions est un mock : on vérifie que le code est exécuté dans une
 * NOUVELLE transaction (REQUIRES_NEW), avec le bon tenant dans {@link TenantContext}, et que le
 * tenant de l'appelant est toujours restauré ensuite, même en cas d'erreur.</p>
 */
@ExtendWith(MockitoExtension.class)
class TenantExecutorTest {

    @Mock private PlatformTransactionManager transactionManager;
    @Mock private TransactionStatus transactionStatus;

    private TenantExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new TenantExecutor(transactionManager);
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    /** Pendant le travail, le tenant est celui demandé ; la transaction est REQUIRES_NEW puis validée. */
    @Test
    void inTenant_ShouldRunWorkInNewTransactionWithRequestedTenant() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        AtomicReference<String> tenantDuringWork = new AtomicReference<>();

        String result = executor.inTenant("ae_ecole_abc123", () -> {
            tenantDuringWork.set(TenantContext.getTenantId());
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals("ae_ecole_abc123", tenantDuringWork.get());

        ArgumentCaptor<TransactionDefinition> captor = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(captor.capture());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, captor.getValue().getPropagationBehavior());
        verify(transactionManager).commit(transactionStatus);
    }

    /** Le tenant de l'appelant (ici « public ») est restauré après l'exécution. */
    @Test
    void inTenant_ShouldRestorePreviousTenant() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        TenantContext.setTenantId("public");

        executor.runInTenant("ae_ecole_abc123", () -> { });

        assertEquals("public", TenantContext.getTenantId());
    }

    /** Sans tenant avant l'appel, le contexte est vidé après (pas de valeur qui « traîne »). */
    @Test
    void inTenant_WithoutPreviousTenant_ShouldClearContext() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        executor.runInTenant("ae_ecole_abc123", () -> { });

        assertNull(TenantContext.getTenantId());
    }

    /** En cas d'erreur : la transaction est annulée et le tenant précédent est quand même restauré. */
    @Test
    void inTenant_WhenWorkFails_ShouldRollbackAndRestoreTenant() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        TenantContext.setTenantId("public");

        assertThrows(IllegalStateException.class, () -> executor.runInTenant("ae_ecole_abc123", () -> {
            throw new IllegalStateException("panne");
        }));

        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
        assertEquals("public", TenantContext.getTenantId());
    }

    /** Nom de schéma dangereux (injection SQL) : refusé AVANT d'ouvrir une transaction. */
    @Test
    void inTenant_WithInvalidSchema_ShouldBeRefusedBeforeAnyTransaction() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.runInTenant("x;DROP SCHEMA public", () -> fail("ne doit pas s'exécuter")));

        verifyNoInteractions(transactionManager);
        assertNull(TenantContext.getTenantId());
    }
}
