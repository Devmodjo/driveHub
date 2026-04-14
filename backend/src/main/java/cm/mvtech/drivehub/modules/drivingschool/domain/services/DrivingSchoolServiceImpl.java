package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolMapper;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.core.domain.service.TenantProvisioningService;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingSchoolServiceImpl implements DrivingSchoolService {


    private final DrivingSchoolRepository drivingSchoolRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    private final DrivingSchoolMapper mapper;

    @Transactional
    @Override
    public void createSchool(DrivingSchoolRequestDto req, UserPrincipal userPrincipal) throws IllegalAccessException {
        
        Optional<User> admin = userRepository.findById(userPrincipal.getId());

        if (admin.isEmpty()) {
            throw new UsernameNotFoundException("cet utilisateur n'existe pas !");
        }

        DrivingSchoolRegistry dr = getDrivingSchoolRegistry(req, admin);
        drivingSchoolRegistryRepository.save(dr);

    }

    private static DrivingSchoolRegistry getDrivingSchoolRegistry(DrivingSchoolRequestDto req, Optional<User> admin) {
        if (admin.get().getRoles() != Role.MONITOR) {
            throw new AccessDeniedException("seul les Encadreur peuvent cree des auto écoles");
        }

        // generation du nom du schema de base de donnée
        String schemaName = req.name().toLowerCase().replaceAll("[^a-z0-9]", "_");

        // creation du tenant(auto-ecole) public
        DrivingSchoolRegistry dr = new DrivingSchoolRegistry();
        dr.setSchoolName(req.name());
        dr.setSchemaName(schemaName);
        dr.setAdmin(admin.get());
        dr.setAddress(req.address());
        dr.setPhoneNumber(req.phoneNumber());
        dr.setDescription(req.description());
        dr.setEmail(req.email());
        dr.setCountry(req.country());
        dr.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        dr.setCity(req.city());
        dr.setWebsiteUrl(req.websiteUrl());
        dr.setWhatsappNumber(req.whatsappNumber());
        return dr;
    }

    @Override
    public List<DrivingSchoolResponseDto> retreiveSchool() {

        List<DrivingSchoolResponseDto> list = new ArrayList<>();

        drivingSchoolRepository.findAll().forEach(
                (DrivingSchool e) -> list.add(
                        new DrivingSchoolResponseDto(e.getId(), e.getName(),
                                e.getPhoneNumber(), e.getAddress(), e.getDescription(), e.getCreatedAt())
                )
        );
        return list;
    }


    @Override
    @Transactional
    public void approveRegistry(UUID registryId) {
        Optional<DrivingSchoolRegistry> drivingSchoolRegistry = Optional.ofNullable(drivingSchoolRegistryRepository.findById(registryId).orElseThrow(
                () -> {
                    throw new IllegalArgumentException("ce auto-ecole n'existe pas dans les registres");
                }
        ));
        DrivingSchoolRegistry schoolRegistry = drivingSchoolRegistry.get();

        User monitor = schoolRegistry.getAdmin();

        if (monitor.getRoles() != Role.MONITOR) {
            throw new AccessDeniedException("cet utilisateur n'est pas un Moniteur");
        }

        String schemaName = schoolRegistry.getSchemaName();
        String schoolName = schoolRegistry.getSchoolName();

        // creation du schema
        tenantProvisioningService.createTenantSchema(schemaName);
        // activation du moniteur
        monitor.setProfileStatus(ProfileStatus.ACTIVE);

        // switch vers le tenant
        TenantContext.setTenantId(schemaName);

        try {
            DrivingSchool ds = getDrivingSchool(schoolRegistry, monitor);
            drivingSchoolRepository.save(ds);
        } finally {
            TenantContext.clear();
        }

        schoolRegistry.setDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        drivingSchoolRegistryRepository.save(schoolRegistry);
    }

    @Override
    public List<DrivingSchoolPendingRequestDTO> retreivePendingRequest() {

        List<DrivingSchoolPendingRequestDTO> list = new ArrayList<>();

        for (DrivingSchoolRegistry registry : drivingSchoolRegistryRepository.findByDrivingSchoolStatus(DrivingSchoolStatus.PENDING)) {
            list.add(new DrivingSchoolPendingRequestDTO(
                    registry.getId(),
                    registry.getSchoolName(),
                    registry.getCountry(),
                    registry.getCity(),
                    registry.getAddress(),
                    registry.getWhatsappNumber(),
                    "%s %s".formatted(registry.getAdmin().getFirstname(), registry.getAdmin().getLastname()),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getResidenceCity).orElse(null),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getNationality).orElse(null),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getGender).orElse(null)
            ));
        }

        return list;
    }

    private static DrivingSchool getDrivingSchool(DrivingSchoolRegistry schoolRegistry, User monitor) {
        DrivingSchool ds = new DrivingSchool();
        ds.setName(schoolRegistry.getSchoolName());
        ds.setAddress(schoolRegistry.getAddress());
        ds.setPhoneNumber(schoolRegistry.getPhoneNumber());
        ds.setDescription(schoolRegistry.getDescription());
        ds.setUser(monitor);
        ds.setEmail(schoolRegistry.getEmail());
        ds.setCountry(schoolRegistry.getCountry());
        ds.setCity(schoolRegistry.getCity());
        ds.setWebsiteUrl(schoolRegistry.getWebsiteUrl());
        ds.setWhatsappNumber(schoolRegistry.getWhatsappNumber());
        ds.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        return ds;
    }


}
