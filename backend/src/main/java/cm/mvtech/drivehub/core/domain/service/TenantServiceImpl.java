package cm.mvtech.drivehub.core.domain.service;

import cm.mvtech.drivehub.core.domain.entities.TenantEntity;
import cm.mvtech.drivehub.core.infrastructure.TenantSchemas;
import cm.mvtech.drivehub.core.infrastructure.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public boolean isValidTenant(String tenantId) {

        // Un nom mal formé est rejeté avant même d'interroger la base.
        if (!TenantSchemas.isValid(tenantId)) {
            return false;
        }

        return tenantRepository.findByCode(tenantId)
                .map(TenantEntity::isActive)
                .orElse(false);
    }
}
