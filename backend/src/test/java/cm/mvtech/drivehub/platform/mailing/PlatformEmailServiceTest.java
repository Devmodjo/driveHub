package cm.mvtech.drivehub.platform.mailing;

import cm.mvtech.drivehub.modules.auth.domain.model.MailRecipient;
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
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailRequest;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailResponse;
import cm.mvtech.drivehub.platform.mailing.domain.model.EmailAudience;
import cm.mvtech.drivehub.platform.mailing.domain.model.PlatformEmailCampaign;
import cm.mvtech.drivehub.platform.mailing.domain.services.PlatformEmailService;
import cm.mvtech.drivehub.platform.mailing.infrastructure.repository.PlatformEmailCampaignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de l'envoi d'emails depuis le back-office (aucune base, aucun serveur mail :
 * les dépôts et EmailService sont simulés avec Mockito).
 */
@ExtendWith(MockitoExtension.class)
class PlatformEmailServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DrivingSchoolRegistryRepository registryRepository;
    @Mock private SchoolJoinRequestRepository joinRequestRepository;
    @Mock private PlatformEmailCampaignRepository campaignRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private PlatformEmailService service;

    // ------------------------------------------------------------------ données de test

    private static User user(String firstname, String email, Role role) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setFirstname(firstname);
        u.setEmail(email);
        u.setRoles(role);
        u.setProfileStatus(ProfileStatus.ACTIVE);
        return u;
    }

    private static DrivingSchoolRegistry school(String name, String contactEmail, User owner, DrivingSchoolStatus status) {
        DrivingSchoolRegistry r = new DrivingSchoolRegistry();
        r.setId(UUID.randomUUID());
        r.setSchoolName(name);
        r.setEmail(contactEmail);
        r.setAdmin(owner);
        r.setDrivingSchoolStatus(status);
        return r;
    }

    private static SchoolJoinRequest approvedMember(User member, UUID schoolId) {
        SchoolJoinRequest request = new SchoolJoinRequest();
        request.setUser(member);
        request.setDrivingSchoolId(schoolId);
        request.setJoinStatus(JoinStatus.APPROVED);
        return request;
    }

    // ------------------------------------------------------------------ destinataires

    /** ALL_SCHOOLS : adresse de contact de chaque auto-école, ou celle du responsable si elle est vide. */
    @Test
    void resolveRecipients_AllSchools_UsesContactEmailOrOwnerEmail() {
        User owner = user("Awa", "awa@test.cm", Role.MONITOR);
        when(registryRepository.findAllByDrivingSchoolStatusIn(anyCollection())).thenReturn(List.of(
                school("Horizon", "contact@horizon.cm", owner, DrivingSchoolStatus.ACTIVE),
                school("Le Volant", "", owner, DrivingSchoolStatus.APPROVED)));

        List<MailRecipient> recipients = service.resolveRecipients(EmailAudience.ALL_SCHOOLS, null, null);

        assertEquals(List.of("contact@horizon.cm", "awa@test.cm"), recipients.stream().map(MailRecipient::email).toList());
        assertEquals("Horizon", recipients.get(0).name());
    }

    /** ALL_MONITORS : comptes moniteurs non suspendus. */
    @Test
    void resolveRecipients_AllMonitors_ExcludesSuspendedAccounts() {
        when(userRepository.findAllByRolesAndProfileStatusNot(Role.MONITOR, ProfileStatus.SUSPENDED))
                .thenReturn(List.of(user("Awa", "awa@test.cm", Role.MONITOR)));

        List<MailRecipient> recipients = service.resolveRecipients(EmailAudience.ALL_MONITORS, null, null);

        assertEquals(1, recipients.size());
        verify(userRepository).findAllByRolesAndProfileStatusNot(Role.MONITOR, ProfileStatus.SUSPENDED);
    }

    /** SCHOOL_MEMBERS : le responsable + les membres acceptés, sans doublon. */
    @Test
    void resolveRecipients_SchoolMembers_OwnerAndApprovedMembersWithoutDuplicates() {
        User owner = user("Awa", "awa@test.cm", Role.MONITOR);
        User student = user("Jean", "jean@test.cm", Role.STUDENT);
        DrivingSchoolRegistry horizon = school("Horizon", "contact@horizon.cm", owner, DrivingSchoolStatus.ACTIVE);
        when(registryRepository.findById(horizon.getId())).thenReturn(Optional.of(horizon));
        when(joinRequestRepository.findAllByDrivingSchoolIdAndJoinStatus(horizon.getId(), JoinStatus.APPROVED))
                .thenReturn(List.of(approvedMember(student, horizon.getId()), approvedMember(owner, horizon.getId())));

        List<MailRecipient> recipients = service.resolveRecipients(EmailAudience.SCHOOL_MEMBERS, horizon.getId(), null);

        assertEquals(List.of("awa@test.cm", "jean@test.cm"), recipients.stream().map(MailRecipient::email).toList());
    }

    @Test
    void resolveRecipients_SchoolMembersWithoutSchool_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.resolveRecipients(EmailAudience.SCHOOL_MEMBERS, null, null));
    }

    /** Une auto-école en attente ou suspendue ne peut pas être ciblée. */
    @Test
    void resolveRecipients_SchoolMembersOfPendingSchool_ThrowsNotFound() {
        DrivingSchoolRegistry pending = school("Attente", "x@test.cm", user("A", "a@test.cm", Role.MONITOR),
                DrivingSchoolStatus.PENDING);
        when(registryRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(ResourceNotFoundException.class,
                () -> service.resolveRecipients(EmailAudience.SCHOOL_MEMBERS, pending.getId(), null));
    }

    /** INDIVIDUAL : adresses normalisées en minuscules, doublons supprimés, prénom repris du compte s'il existe. */
    @Test
    void resolveRecipients_Individual_NormalizesAndDeduplicates() {
        when(userRepository.findByEmail("jean@test.cm")).thenReturn(Optional.of(user("Jean", "jean@test.cm", Role.STUDENT)));
        when(userRepository.findByEmail("inconnu@test.cm")).thenReturn(Optional.empty());

        List<MailRecipient> recipients = service.resolveRecipients(EmailAudience.INDIVIDUAL, null,
                List.of(" Jean@Test.cm ", "jean@test.cm", "inconnu@test.cm"));

        assertEquals(2, recipients.size());
        assertEquals(new MailRecipient("jean@test.cm", "Jean"), recipients.get(0));
        assertEquals(new MailRecipient("inconnu@test.cm", ""), recipients.get(1));
    }

    @Test
    void resolveRecipients_IndividualWithoutAddress_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.resolveRecipients(EmailAudience.INDIVIDUAL, null, List.of()));
    }

    // ------------------------------------------------------------------ envoi

    /** L'envoi est enregistré dans l'historique puis confié à EmailService (en arrière-plan). */
    @Test
    void send_SavesCampaignAndSendsInBackground() {
        when(userRepository.findAllByRolesAndProfileStatusNot(Role.STUDENT, ProfileStatus.SUSPENDED))
                .thenReturn(List.of(user("Jean", "jean@test.cm", Role.STUDENT), user("Marie", "marie@test.cm", Role.STUDENT)));
        when(campaignRepository.save(any(PlatformEmailCampaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SendPlatformEmailResponse response = service.send(new SendPlatformEmailRequest(
                EmailAudience.ALL_STUDENTS, null, null, " Rentrée ", " Bonjour à tous, les cours reprennent. "),
                "root@drivehub.cm");

        assertEquals(2, response.recipientCount());
        ArgumentCaptor<PlatformEmailCampaign> saved = ArgumentCaptor.forClass(PlatformEmailCampaign.class);
        verify(campaignRepository).save(saved.capture());
        assertEquals("Rentrée", saved.getValue().getSubject());
        assertEquals("root@drivehub.cm", saved.getValue().getSentByEmail());
        assertEquals(2, saved.getValue().getRecipientCount());
        assertNotNull(saved.getValue().getSentAt());
        verify(emailService).sendPlatformAnnouncement(argThat(list -> list.size() == 2), eq("Rentrée"),
                eq("Bonjour à tous, les cours reprennent."));
    }

    /** Aucun destinataire : rien n'est enregistré ni envoyé. */
    @Test
    void send_WithoutRecipients_ThrowsBadRequestAndSendsNothing() {
        when(userRepository.findAllByRolesAndProfileStatusNot(Role.MONITOR, ProfileStatus.SUSPENDED)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> service.send(new SendPlatformEmailRequest(
                EmailAudience.ALL_MONITORS, null, null, "Objet", "Un message assez long"), "root@drivehub.cm"));
        verifyNoInteractions(campaignRepository, emailService);
    }

    // ------------------------------------------------------------------ aperçu et recherche

    @Test
    void countRecipients_Individual_ReturnsZero() {
        assertEquals(0, service.countRecipients(EmailAudience.INDIVIDUAL, null));
        verifyNoInteractions(userRepository, registryRepository);
    }

    @Test
    void searchRecipients_WithLessThanTwoCharacters_ReturnsNothing() {
        assertTrue(service.searchRecipients(" a ").isEmpty());
        verifyNoInteractions(userRepository, registryRepository);
    }

    /** Recherche : auto-écoles (type SCHOOL) puis comptes, avec l'auto-école de chaque moniteur. */
    @Test
    void searchRecipients_ReturnsSchoolsThenUsers() {
        User owner = user("Awa", "awa@test.cm", Role.MONITOR);
        DrivingSchoolRegistry horizon = school("Horizon", "contact@horizon.cm", owner, DrivingSchoolStatus.ACTIVE);
        when(registryRepository.searchByStatus(eq("awa"), anyCollection(), any())).thenReturn(List.of(horizon));
        when(userRepository.search(eq("awa"), any())).thenReturn(List.of(owner));
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.of(horizon));

        var results = service.searchRecipients("awa");

        assertEquals(2, results.size());
        assertEquals("SCHOOL", results.get(0).type());
        assertEquals("MONITOR", results.get(1).type());
        assertEquals("Horizon", results.get(1).schoolName());
    }
}
