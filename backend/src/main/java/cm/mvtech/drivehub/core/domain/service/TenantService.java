package cm.mvtech.drivehub.core.domain.service;

public interface TenantService {
    /**
     * Vérifie si un tenant existe et est actif.
     *
     * @param tenantId identifiant du tenant
     * @return true si le tenant est valide
     */
    boolean isValidTenant(String tenantId);
}
