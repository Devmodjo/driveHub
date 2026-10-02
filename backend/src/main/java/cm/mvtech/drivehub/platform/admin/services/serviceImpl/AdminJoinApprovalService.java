package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.modules.student.infrastructure.repository.StudentsRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;

import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.PendingJoinRequestResponse;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
    private final TenantExecutor tenantExecutor;
    private final EmailService emailService;

    /**
     * Le moniteur responsable d'une auto-école accepte une demande d'adhésion.
     *
     * <p>Corrections :</p>
     * <ul>
     *   <li>n'importe quel moniteur pouvait approuver n'importe quelle demande : on vérifie maintenant
     *       que l'approbateur est bien le responsable de l'auto-école visée ;</li>
     *   <li>l'auto-école était cherchée dans le tenant avec l'id du REGISTRE (toujours introuvable) ;</li>
     *   <li>la fiche élève était créée vide (champs NOT NULL manquants -> erreur SQL) : on copie
     *       maintenant le profil saisi à l'inscription ;</li>
     *   <li>le changement de tenant se fait dans une nouvelle transaction (TenantExecutor).</li>
     * </ul>
     */
    @Transactional
    public void approve(UUID requestId, String approverEmail) {

        // Charger la demande (PUBLIC)
        SchoolJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        if (request.getJoinStatus() != JoinStatus.PENDING) {
            throw new IllegalStateException("Demande déjà traitée");
        }

        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository.findById(request.getDrivingSchoolId())
                .orElseThrow(() -> new IllegalStateException("Auto-école registry introuvable"));

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        if (!registry.getAdmin().getId().equals(approver.getId())) {
            throw new AccessDeniedException("Vous ne pouvez traiter que les demandes de votre auto-école");
        }

        User user = request.getUser();
        Student studentProfile = user.getStudents().stream().findFirst().orElse(null);
        Monitor monitorProfile = user.getMonitors().stream().findFirst().orElse(null);

        // Création de la fiche métier DANS LE SCHÉMA DE L'AUTO-ÉCOLE
        tenantExecutor.runInTenant(registry.getSchemaName(), () -> {
            DrivingSchool school = drivingSchoolRepository.findFirstByOrderByCreatedAtAsc()
                    .orElseThrow(() -> new IllegalStateException("Auto-école introuvable dans son schéma"));

            if (request.getRequestedRole() == Role.STUDENT) {
                if (studentProfile == null) {
                    throw new IllegalStateException("Profil élève introuvable pour " + user.getEmail());
                }
                studentRepository.save(copyStudent(studentProfile, user));
            }

            if (request.getRequestedRole() == Role.MONITOR) {
                if (monitorProfile == null) {
                    throw new IllegalStateException("Profil moniteur introuvable pour " + user.getEmail());
                }
                monitorRepository.save(copyMonitor(monitorProfile, user, school));
            }
        });

        // Mise à jour utilisateur (PUBLIC)
        user.setProfileStatus(ProfileStatus.ACTIVE);
        userRepository.save(user);

        // Clôture de la demande
        request.setJoinStatus(JoinStatus.APPROVED);
        joinRequestRepository.save(request);

        // Prévenir le demandeur
        emailService.sendJoinApprovedEmail(user.getEmail(), user.getFirstname(), registry.getSchoolName());
    }

    @Transactional
    public void reject(UUID requestId, String approverEmail) {
        SchoolJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));
        if (request.getJoinStatus() != JoinStatus.PENDING) {
            throw new IllegalStateException("Demande déjà traitée");
        }
        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository.findById(request.getDrivingSchoolId())
                .orElseThrow(() -> new IllegalStateException("Auto-école registry introuvable"));
        // Même contrôle que approve() : par identifiant (la comparaison d'emails était sensible à la casse)
        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        if (!registry.getAdmin().getId().equals(approver.getId())) {
            throw new AccessDeniedException("Vous ne pouvez traiter que les demandes de votre auto-école");
        }
        request.setJoinStatus(JoinStatus.REJECTED);
        joinRequestRepository.save(request);

        User user = request.getUser();
        emailService.sendJoinRejectedEmail(user.getEmail(), user.getFirstname(), registry.getSchoolName());
    }

    private static Student copyStudent(Student source, User user) {
        Student copy = new Student();
        copy.setUser(user);
        copy.setPhoneNumber(source.getPhoneNumber());
        copy.setDateOfBirth(source.getDateOfBirth());
        copy.setGender(source.getGender());
        copy.setNationality(source.getNationality());
        copy.setResidenceCity(source.getResidenceCity());
        copy.setLicenseCategory(source.getLicenseCategory());
        copy.setCniRectoUrl(source.getCniRectoUrl());
        copy.setCniVersoUrl(source.getCniVersoUrl());
        return copy;
    }

    private static Monitor copyMonitor(Monitor source, User user, DrivingSchool school) {
        Monitor copy = new Monitor();
        copy.setUser(user);
        copy.setDrivingSchool(school);
        copy.setPhoneNumber(source.getPhoneNumber());
        copy.setDateOfBirth(source.getDateOfBirth());
        copy.setGender(source.getGender());
        copy.setNationality(source.getNationality());
        copy.setResidenceCity(source.getResidenceCity());
        return copy;
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
