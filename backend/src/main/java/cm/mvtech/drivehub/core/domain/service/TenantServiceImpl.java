package cm.mvtech.drivehub.core.domain.service;

import cm.mvtech.drivehub.core.domain.entities.TenantEntity;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.core.infrastructure.repository.TenantRepository;
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
