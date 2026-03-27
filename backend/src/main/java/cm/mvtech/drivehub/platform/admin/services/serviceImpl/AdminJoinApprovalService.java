package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.student.Student;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.monitor.infrastucture.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.monitor.infrastucture.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.modules.student.StudentsRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.PendingJoinRequestResponse;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminJoinApprovalService {

    private final SchoolJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final StudentsRepository studentRepository;
    private final MonitorsRepository monitorRepository;
    private final DrivingSchoolRepository drivingSchoolRepository;
    private final DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;

    @Transactional
    public void approve(UUID requestId) {

        // Charger la demande (PUBLIC)
        SchoolJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        if (request.getJoinStatus() != JoinStatus.PENDING) {
            throw new IllegalStateException("Demande déjà traitée");
        }

        User user = request.getUser();

        Optional<DrivingSchoolRegistry> registry = Optional.ofNullable(drivingSchoolRegistryRepository.findById(request.getDrivingSchoolId())
                .orElseThrow(() -> new IllegalStateException("Auto-école registry introuvable")));

        // Switch vers le tenant
        TenantContext.setTenantId(registry.get().getSchemaName());

        try {

            // Charger l’auto-école DANS LE TENANT
            DrivingSchool school = drivingSchoolRepository.findById(
                    request.getDrivingSchoolId()
            ).orElseThrow(() -> new IllegalStateException("Auto-école introuvable"));

            // Création métier
            if (request.getRequestedRole() == Role.STUDENT) {
                Student student = new Student();
                student.setUser(user);
                studentRepository.save(student);
            }

            if (request.getRequestedRole() == Role.MONITOR) {
                Monitor monitor = new Monitor();
                monitor.setUser(user);
                monitor.setDrivingSchool(school);
                monitorRepository.save(monitor);
            }

        } finally {
            TenantContext.clear();
        }

        // Mise à jour utilisateur (PUBLIC)
        user.setProfileStatus(ProfileStatus.ACTIVE);
        userRepository.save(user);

        // Clôture de la demande
        request.setJoinStatus(JoinStatus.APPROVED);
        joinRequestRepository.save(request);
    }


    @Transactional
    public ApiPageResponse<PendingJoinRequestResponse> getPendingRequests(
            String adminEmail,
            int page,
            int size
    ) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Admin introuvable"));

        DrivingSchoolRegistry registry =
                drivingSchoolRegistryRepository.findByAdmin(admin)
                        .orElseThrow(() ->
                                new IllegalStateException("Admin sans auto-école"));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdOn").descending()
        );

        Page<PendingJoinRequestResponse> pageResult =
                joinRequestRepository
                        .findByDrivingSchoolIdAndJoinStatus(
                                registry.getId(),
                                JoinStatus.PENDING,
                                pageable
                        )
                        .map(request -> toResponse(request, registry));

        return ApiPageResponse.from(pageResult);
    }


    private PendingJoinRequestResponse toResponse(
            SchoolJoinRequest request,
            DrivingSchoolRegistry registry
    ) {
        User user = request.getUser();

        return new PendingJoinRequestResponse(
                request.getId(),
                user.getId(),
                user.getFirstname(),
                user.getEmail(),
                request.getRequestedRole(),
                registry.getId(),
                registry.getSchoolName(),
                request.getCreatedOn()
        );
    }


}
