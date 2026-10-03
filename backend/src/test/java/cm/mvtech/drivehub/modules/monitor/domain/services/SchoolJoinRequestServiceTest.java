package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.JoinSchoolRequestDto;
import cm.mvtech.drivehub.modules.messageapi.MyJoinRequestResponse;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link SchoolJoinRequestService} : un élève (ou un moniteur) demande à
 * rejoindre une auto-école validée.
 *
 * <p>Règles vérifiées : email vérifié obligatoire, rôle demandé = rôle du compte, auto-école
 * existante et validée, pas de double demande en attente, email envoyé au responsable.</p>
 */
@ExtendWith(MockitoExtension.class)
class SchoolJoinRequestServiceTest {

    @Mock private SchoolJoinRequestRepository joinRequestRepository;
    @Mock private DrivingSchoolRegistryRepository registryRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private DocumentService documentService;

    @InjectMocks
    private SchoolJoinRequestService service;

    private User student;
    private User owner;
    private DrivingSchoolRegistry registry;

    @BeforeEach
    void setUp() {
        student = TestData.user(Role.STUDENT);
        owner = TestData.user(Role.MONITOR);
        registry = TestData.registry(owner, DrivingSchoolStatus.APPROVED);
    }

    private JoinSchoolRequestDto requestFor(DrivingSchoolRegistry target) {
        return new JoinSchoolRequestDto(target.getId(), Role.STUDENT);
    }

    // ─── requestJoin : cas nominal ────────────────────────────────────────────

    /**
     * Cas nominal : la demande est enregistrée PENDING avec l'id du REGISTRE et le rôle du compte,
     * puis le responsable de l'auto-école est prévenu par email.
     */
    @Test
    void requestJoin_Success_ShouldSavePendingRequestAndNotifyOwner() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(joinRequestRepository.existsByUserAndDrivingSchoolIdAndJoinStatus(student, registry.getId(), JoinStatus.PENDING))
                .thenReturn(false);

        service.requestJoin(student.getEmail(), requestFor(registry));

        ArgumentCaptor<SchoolJoinRequest> captor = ArgumentCaptor.forClass(SchoolJoinRequest.class);
        verify(joinRequestRepository).save(captor.capture());
        SchoolJoinRequest saved = captor.getValue();
        assertSame(student, saved.getUser());
        assertEquals(registry.getId(), saved.getDrivingSchoolId());
        assertEquals(Role.STUDENT, saved.getRequestedRole());
        assertEquals(JoinStatus.PENDING, saved.getJoinStatus());

        verify(emailService).sendJoinRequestReceivedEmail(
                eq(owner.getEmail()), eq(owner.getFirstname()), anyString(), eq(registry.getSchoolName()));
    }

    /** Une auto-école ACTIVE accepte aussi les demandes (pas seulement APPROVED). */
    @Test
    void requestJoin_ActiveSchool_ShouldBeAccepted() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        service.requestJoin(student.getEmail(), requestFor(registry));

        verify(joinRequestRepository).save(any(SchoolJoinRequest.class));
    }

    // ─── requestJoin : cas d'erreur ───────────────────────────────────────────

    @Test
    void requestJoin_UnknownUser_ShouldThrowUsernameNotFound() {
        when(userRepository.findByEmail("inconnu@test.cm")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.requestJoin("inconnu@test.cm", requestFor(registry)));
        verify(joinRequestRepository, never()).save(any());
    }

    /** Email non vérifié (REGISTERED) ou déjà membre d'une auto-école (ACTIVE) : refus. */
    @Test
    void requestJoin_EmailNotVerified_ShouldThrowBadRequest() {
        student.setProfileStatus(ProfileStatus.REGISTERED);
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThrows(BadRequestException.class, () -> service.requestJoin(student.getEmail(), requestFor(registry)));

        student.setProfileStatus(ProfileStatus.ACTIVE);
        assertThrows(BadRequestException.class, () -> service.requestJoin(student.getEmail(), requestFor(registry)));
        verify(joinRequestRepository, never()).save(any());
    }

    /** Un élève ne peut pas demander à entrer comme moniteur. */
    @Test
    void requestJoin_RoleDifferentFromAccount_ShouldThrowBadRequest() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThrows(BadRequestException.class, () -> service.requestJoin(student.getEmail(),
                new JoinSchoolRequestDto(registry.getId(), Role.MONITOR)));
        verify(joinRequestRepository, never()).save(any());
    }

    @Test
    void requestJoin_WithoutSchoolId_ShouldThrowBadRequest() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThrows(BadRequestException.class, () -> service.requestJoin(student.getEmail(),
                new JoinSchoolRequestDto(null, Role.STUDENT)));
    }

    @Test
    void requestJoin_UnknownSchool_ShouldThrowResourceNotFound() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requestJoin(student.getEmail(), requestFor(registry)));
    }

    /** Une auto-école encore en attente de validation par la plateforme n'est pas joignable. */
    @Test
    void requestJoin_SchoolNotYetApproved_ShouldThrowResourceNotFound() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        assertThrows(ResourceNotFoundException.class, () -> service.requestJoin(student.getEmail(), requestFor(registry)));
        verify(joinRequestRepository, never()).save(any());
    }

    @Test
    void requestJoin_PendingRequestAlreadyExists_ShouldThrowConflict() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(joinRequestRepository.existsByUserAndDrivingSchoolIdAndJoinStatus(student, registry.getId(), JoinStatus.PENDING))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> service.requestJoin(student.getEmail(), requestFor(registry)));
        verify(joinRequestRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // ─── myRequests ───────────────────────────────────────────────────────────

    /** Les demandes sont enrichies du nom de l'auto-école ; un registre disparu donne un nom vide. */
    @Test
    void myRequests_ShouldEnrichWithSchoolNameAndCity() {
        SchoolJoinRequest known = new SchoolJoinRequest(student, registry.getId(), Role.STUDENT, JoinStatus.PENDING);
        known.setId(UUID.randomUUID());
        UUID vanishedSchool = UUID.randomUUID();
        SchoolJoinRequest orphan = new SchoolJoinRequest(student, vanishedSchool, Role.STUDENT, JoinStatus.REJECTED);
        orphan.setId(UUID.randomUUID());

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(joinRequestRepository.findAllByUserOrderByCreatedOnDesc(student)).thenReturn(List.of(known, orphan));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));
        when(registryRepository.findById(vanishedSchool)).thenReturn(Optional.empty());

        List<MyJoinRequestResponse> result = service.myRequests(student.getEmail());

        assertEquals(2, result.size());
        assertEquals(registry.getSchoolName(), result.get(0).drivingSchoolName());
        assertEquals("Douala", result.get(0).city());
        assertEquals(JoinStatus.PENDING, result.get(0).joinStatus());
        assertNull(result.get(1).drivingSchoolName());
        assertEquals(JoinStatus.REJECTED, result.get(1).joinStatus());
    }

    @Test
    void myRequests_UnknownUser_ShouldThrowUsernameNotFound() {
        when(userRepository.findByEmail("inconnu@test.cm")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.myRequests("inconnu@test.cm"));
    }
}
