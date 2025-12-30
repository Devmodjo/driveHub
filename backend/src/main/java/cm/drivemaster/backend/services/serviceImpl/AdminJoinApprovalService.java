package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.*;
import cm.drivemaster.backend.core.TenantContext;
import cm.drivemaster.backend.enums.JoinStatus;
import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.models.dto.ApiPageResponse;
import cm.drivemaster.backend.models.dto.PendingJoinRequestResponse;
import cm.drivemaster.backend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public void approve(Long requestId) {

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
                user.getName(),
                user.getEmail(),
                request.getRequestedRole(),
                registry.getId(),
                registry.getSchoolName(),
                request.getCreatedOn()
        );
    }


}
