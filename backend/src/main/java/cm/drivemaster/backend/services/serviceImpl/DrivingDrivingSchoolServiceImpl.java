package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.DrivingSchool;
import cm.drivemaster.backend.beans.DrivingSchoolRegistry;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.core.TenantContext;
import cm.drivemaster.backend.enums.DrivingSchoolStatus;
import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;
import cm.drivemaster.backend.models.dto.DrivingSchoolResponseDto;
import cm.drivemaster.backend.models.mappers.DrivingSchoolMapper;
import cm.drivemaster.backend.repositories.DrivingSchoolRegistryRepository;
import cm.drivemaster.backend.repositories.DrivingSchoolRepository;
import cm.drivemaster.backend.repositories.UserRepository;
import cm.drivemaster.backend.services.DrivingSchoolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DrivingDrivingSchoolServiceImpl implements DrivingSchoolService {


    private final DrivingSchoolRepository drivingSchoolRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    private final DrivingSchoolMapper mapper;

    @Transactional
    @Override
    public void createSchool(DrivingSchoolRequestDto req, long userId) throws IllegalAccessException {

        Optional<User> admin = userRepository.findById(userId);

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
                (e) -> list.add(mapper.fromEntityToResponse(e))
        );
        return list;
    }

    @Override
    @Transactional
    public void approveRegistry(long registryId) {
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
        
        // creation du schema
        tenantProvisioningService.createTenantSchema(schemaName);
        // activation du moniteur
        monitor.setProfileStatus(ProfileStatus.ACTIVE);

        // switch vers le tenant
        TenantContext.setTenantId(schemaName);

        try {
            DrivingSchool ds = getDrivingSchool(schemaName, schoolRegistry, monitor);
            drivingSchoolRepository.save(ds);
        } finally {
            TenantContext.clear();
        }

        schoolRegistry.setDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        drivingSchoolRegistryRepository.save(schoolRegistry);
    }

    private static DrivingSchool getDrivingSchool(String schemaName, DrivingSchoolRegistry schoolRegistry, User monitor) {
        DrivingSchool ds = new DrivingSchool();
        ds.setName(schemaName);
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
