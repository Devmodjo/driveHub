package cm.mvtech.drivehub.core.infrastructure;


import org.springframework.stereotype.Component;

/**
 * classe permettant de stocker le tenant courant
 * pendant une requete requête
 * elle enregistrer l'identifiant du tenant pour le thread en cours
 */

@Component
public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
