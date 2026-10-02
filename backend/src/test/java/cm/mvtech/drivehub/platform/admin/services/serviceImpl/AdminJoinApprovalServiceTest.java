package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.PendingJoinRequestResponse;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.infrastructure.repository.StudentsRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link AdminJoinApprovalService} : le moniteur RESPONSABLE d'une auto-école
 * accepte ou refuse les demandes d'adhésion.
 *
 * <p>À l'acceptation, la fiche métier (élève ou moniteur) est copiée DANS le schéma de l'auto-école
 * via {@link TenantExecutor}. Ici le TenantExecutor est un mock qui exécute directement le code
 * reçu : on vérifie ce qui est sauvegardé sans base de données.</p>
 */
@ExtendWith(MockitoExtension.class)
class AdminJoinApprovalServiceTest {

    @Mock private SchoolJoinRequestRepository joinRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudentsRepository studentRepository;
    @Mock private MonitorsRepository monitorRepository;
    @Mock private DrivingSchoolRepository drivingSchoolRepository;
    @Mock private DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    @Mock private TenantExecutor tenantExecutor;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminJoinApprovalService service;

    private User owner;
    private User applicant;
    private Student publicStudentProfile;
    private DrivingSchoolRegistry registry;
    private DrivingSchool school;
    private SchoolJoinRequest request;

    @BeforeEach
    void setUp() {
        owner = TestData.user(Role.MONITOR);
        applicant = TestData.user(Role.STUDENT);
        publicStudentProfile = TestData.student(applicant);
        applicant.setStudents(Set.of(publicStudentProfile));

        registry = TestData.registry(owner, DrivingSchoolStatus.APPROVED);
        school = TestData.school();

        request = new SchoolJoinRequest(applicant, registry.getId(), Role.STUDENT, JoinStatus.PENDING);
        request.setId(UUID.randomUUID());
    }

    /** Le mock TenantExecutor exécute simplement le code reçu (sans changer de schéma). */
    private void givenTenantExecutorRunsWork() {
        doAnswer(inv -> {
            inv.<Runnable>getArgument(1).run();
            return null;
        }).when(tenantExecutor).runInTenant(anyString(), any(Runnable.class));
    }

    private void givenPendingRequestOfOwnedSchool() {
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    }

    // ─── approve ──────────────────────────────────────────────────────────────

    /**
     * Cas nominal (élève) : le profil saisi à l'inscription est copié dans le schéma de l'auto-école,
     * le compte devient ACTIVE, la demande APPROVED, et l'élève reçoit un email.
     */
    @Test
    void approve_StudentRequest_ShouldCopyProfileInTenantAndNotify() {
        givenPendingRequestOfOwnedSchool();
        givenTenantExecutorRunsWork();
        when(drivingSchoolRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(school));

        service.approve(request.getId(), owner.getEmail());

        verify(tenantExecutor).runInTenant(eq(registry.getSchemaName()), any(Runnable.class));
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student copy = captor.getValue();
        assertSame(applicant, copy.getUser());
        assertNull(copy.getId(), "la copie est une NOUVELLE ligne dans le schéma du tenant");
        assertEquals(publicStudentProfile.getPhoneNumber(), copy.getPhoneNumber());
        assertEquals(publicStudentProfile.getGender(), copy.getGender());
        assertEquals(publicStudentProfile.getResidenceCity(), copy.getResidenceCity());
        verify(monitorRepository, never()).save(any());

        assertEquals(ProfileStatus.ACTIVE, applicant.getProfileStatus());
        assertEquals(JoinStatus.APPROVED, request.getJoinStatus());
        verify(userRepository).save(applicant);
        verify(joinRequestRepository).save(request);
        verify(emailService).sendJoinApprovedEmail(applicant.getEmail(), applicant.getFirstname(), registry.getSchoolName());
    }

    /** Cas nominal (moniteur) : la fiche moniteur copiée est rattachée à l'auto-école du tenant. */
    @Test
    void approve_MonitorRequest_ShouldCopyMonitorAttachedToSchool() {
        User monitorApplicant = TestData.user(Role.MONITOR);
        Monitor publicMonitorProfile = TestData.monitor(monitorApplicant);
        monitorApplicant.setMonitors(Set.of(publicMonitorProfile));
        request.setUser(monitorApplicant);
        request.setRequestedRole(Role.MONITOR);
        givenPendingRequestOfOwnedSchool();
        givenTenantExecutorRunsWork();
        when(drivingSchoolRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(school));

        service.approve(request.getId(), owner.getEmail());

        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorRepository).save(captor.capture());
        assertSame(monitorApplicant, captor.getValue().getUser());
        assertSame(school, captor.getValue().getDrivingSchool());
        assertEquals(publicMonitorProfile.getPhoneNumber(), captor.getValue().getPhoneNumber());
        verify(studentRepository, never()).save(any());
        assertEquals(JoinStatus.APPROVED, request.getJoinStatus());
    }

    @Test
    void approve_UnknownRequest_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(joinRequestRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approve(unknown, owner.getEmail()));
        verifyNoInteractions(tenantExecutor, emailService);
    }

    @Test
    void approve_AlreadyProcessedRequest_ShouldThrowIllegalState() {
        request.setJoinStatus(JoinStatus.REJECTED);
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> service.approve(request.getId(), owner.getEmail()));
        verifyNoInteractions(tenantExecutor, emailService);
    }

    /** Contrôle de propriété : un moniteur d'une AUTRE auto-école ne peut pas approuver. */
    @Test
    void approve_ByMonitorWhoIsNotTheOwner_ShouldThrowAccessDenied() {
        User otherMonitor = TestData.user(Role.MONITOR);
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(userRepository.findByEmail(otherMonitor.getEmail())).thenReturn(Optional.of(otherMonitor));

        assertThrows(AccessDeniedException.class, () -> service.approve(request.getId(), otherMonitor.getEmail()));
        verifyNoInteractions(tenantExecutor, emailService);
        assertEquals(JoinStatus.PENDING, request.getJoinStatus());
    }

    @Test
    void approve_UnknownApprover_ShouldThrowUsernameNotFound() {
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(userRepository.findByEmail("inconnu@test.cm")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.approve(request.getId(), "inconnu@test.cm"));
        verifyNoInteractions(tenantExecutor);
    }

    @Test
    void approve_RegistryMissing_ShouldThrowIllegalState() {
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.approve(request.getId(), owner.getEmail()));
    }

    /** Élève sans profil public (données incohérentes) : erreur, la demande reste PENDING. */
    @Test
    void approve_StudentWithoutProfile_ShouldThrowAndKeepRequestPending() {
        applicant.setStudents(Set.of());
        givenPendingRequestOfOwnedSchool();
        givenTenantExecutorRunsWork();
        when(drivingSchoolRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(school));

        assertThrows(IllegalStateException.class, () -> service.approve(request.getId(), owner.getEmail()));
        assertEquals(JoinStatus.PENDING, request.getJoinStatus());
        verifyNoInteractions(emailService);
    }

    // ─── reject ───────────────────────────────────────────────────────────────

    @Test
    void reject_ByOwner_ShouldRejectAndNotify() {
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        service.reject(request.getId(), owner.getEmail());

        assertEquals(JoinStatus.REJECTED, request.getJoinStatus());
        verify(joinRequestRepository).save(request);
        verify(emailService).sendJoinRejectedEmail(applicant.getEmail(), applicant.getFirstname(), registry.getSchoolName());
        verifyNoInteractions(tenantExecutor);
    }

    @Test
    void reject_ByMonitorWhoIsNotTheOwner_ShouldThrowAccessDenied() {
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(drivingSchoolRegistryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        assertThrows(AccessDeniedException.class, () -> service.reject(request.getId(), "autre-moniteur@test.cm"));
        assertEquals(JoinStatus.PENDING, request.getJoinStatus());
        verifyNoInteractions(emailService);
    }

    @Test
    void reject_AlreadyProcessedRequest_ShouldThrowIllegalState() {
        request.setJoinStatus(JoinStatus.APPROVED);
        when(joinRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> service.reject(request.getId(), owner.getEmail()));
        verifyNoInteractions(emailService);
    }

    // ─── getPendingRequests ───────────────────────────────────────────────────

    @Test
    void getPendingRequests_ShouldListPendingRequestsOfOwnerSchool() {
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(drivingSchoolRegistryRepository.findByAdmin(owner)).thenReturn(Optional.of(registry));
        when(joinRequestRepository.findByDrivingSchoolIdAndJoinStatus(eq(registry.getId()), eq(JoinStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request)));

        ApiPageResponse<PendingJoinRequestResponse> page = service.getPendingRequests(owner.getEmail(), 0, 10);

        assertEquals(1, page.content().size());
        PendingJoinRequestResponse item = page.content().get(0);
        assertEquals(request.getId(), item.requestId());
        assertEquals(applicant.getEmail(), item.userEmail());
        assertEquals(Role.STUDENT, item.role());
        assertEquals(registry.getSchoolName(), item.drivingSchoolName());
    }

    /** Un moniteur qui ne gère aucune auto-école n'a pas de demandes à consulter. */
    @Test
    void getPendingRequests_MonitorWithoutSchool_ShouldThrowIllegalState() {
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(drivingSchoolRegistryRepository.findByAdmin(owner)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.getPendingRequests(owner.getEmail(), 0, 10));
    }
}
