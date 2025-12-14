package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.TenantEntity;
import cm.drivemaster.backend.repositories.TenantRepository;
import cm.drivemaster.backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public boolean isValidTenant(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }

        return tenantRepository.findByCode(tenantId)
                .map(TenantEntity::isActive)
                .orElse(false);
    }
}
