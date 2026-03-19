package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.TenantEntity;
import cm.drivemaster.backend.repositories.DrivingSchoolRegistryRepository;
import cm.drivemaster.backend.repositories.TenantRepository;
import cm.drivemaster.backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final DrivingSchoolRegistryRepository registryRepository;

    @Override
    public boolean isValidTenant(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }


//        return registryRepository.existsBySchemaName(tenantId);
        return tenantRepository.findByCode(tenantId)
                .map(TenantEntity::isActive)
                .orElse(false);
    }
}
