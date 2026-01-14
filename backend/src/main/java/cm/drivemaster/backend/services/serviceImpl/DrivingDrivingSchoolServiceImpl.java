package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.DrivingSchool;
import cm.drivemaster.backend.beans.DrivingSchoolRegistry;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.core.TenantContext;
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

        if (!admin.isPresent()) {
            throw new UsernameNotFoundException("cet utilisateur n'existe pas !");
        }

        if (admin.get().getRoles() != Role.ADMIN) {
            throw new AccessDeniedException("seul les admin peuvent cree des auto écoles");
        }

        // generation du nom du schema de base de donnée
        String schemaName = req.name().toLowerCase().replaceAll("[^a-z0-9]", "_");

        // creation du schema postGres
        tenantProvisioningService.createTenantSchema(schemaName);

        // creation du tenant(auto-ecole) public
        DrivingSchoolRegistry dr = new DrivingSchoolRegistry();
        dr.setSchoolName(req.name());
        dr.setSchemaName(schemaName);
        dr.setAdmin(admin.get());
        drivingSchoolRegistryRepository.save(dr);

        // switch vers le tenant
        TenantContext.setTenantId(schemaName);

        try {

            // creation de l'auto ecole metier
            DrivingSchool ds = new DrivingSchool();
            ds.setName(req.name());
            ds.setAddress(req.address());
            ds.setPhoneNumber(req.phoneNumber());
            ds.setDescription(req.description());
            ds.setUser(admin.get());

            drivingSchoolRepository.save(ds);

            // activation de l'admin apre creation de son établissement
            admin.get().setProfileStatus(ProfileStatus.ACTIVE);
            admin.get().setFullProfile(true);
            userRepository.save(admin.get());

        } finally {
            TenantContext.clear();
        }

    }

    @Override
    public List<DrivingSchoolResponseDto> retreiveSchool() {

        List<DrivingSchoolResponseDto> list = new ArrayList<>();

        drivingSchoolRepository.findAll().forEach(
                (e) -> list.add(mapper.fromEntityToResponse(e))
        );
        return list;
    }


}
