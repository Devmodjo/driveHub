package cm.mvtech.drivehub.core.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Exécute du code dans le schéma d'un tenant donné.
 *
 * <p>POURQUOI CETTE CLASSE ? Hibernate choisit le schéma au moment où il OUVRE la session
 * (début de transaction). Appeler {@code TenantContext.setTenantId(...)} au milieu d'une
 * méthode {@code @Transactional} n'a donc AUCUN effet : la session déjà ouverte reste sur
 * {@code public}. Il faut ouvrir une NOUVELLE transaction (REQUIRES_NEW) après avoir changé
 * le tenant : c'est ce que fait cette classe.</p>
 *
 * <pre>
 * DrivingSchool school = tenantExecutor.inTenant(schema, () -> drivingSchoolRepository.save(ds));
 * </pre>
 */
@Component
public class TenantExecutor {

    private final TransactionTemplate requiresNew;

    public TenantExecutor(PlatformTransactionManager transactionManager) {
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public <T> T inTenant(String schema, Supplier<T> work) {
        TenantSchemas.requireValid(schema);
        String previous = TenantContext.getTenantId();
        TenantContext.setTenantId(schema);
        try {
            return requiresNew.execute(status -> work.get());
        } finally {
            // On restaure le tenant précédent (et non un simple clear) pour ne pas
            // casser le contexte de l'appelant.
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(previous);
            }
        }
    }

    public void runInTenant(String schema, Runnable work) {
        inTenant(schema, () -> {
            work.run();
            return null;
        });
    }
}
