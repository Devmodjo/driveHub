package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.auth.application.dto.AcceptInvitationRequest;
import cm.mvtech.drivehub.modules.auth.domain.model.PasswordResetToken;
import cm.mvtech.drivehub.modules.auth.domain.model.PrivacyPolicy;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.PasswordResetTokenRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;
import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.document.domain.services.SchoolDocumentAccessService;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.monitor.application.dto.AddMonitorRequest;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.mapper.MonitorMapper;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitorInvitationServiceTest {

    @Mock private SchoolDocumentAccessService schoolAccess;
    @Mock private DrivingSchoolRegistryRepository registryRepository;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Mock private UserRepository userRepository;
    @Mock private MonitorsRepository monitorsRepository;
    @Mock private SchoolJoinRequestRepository joinRequestRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private DocumentService documentService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MonitorMapper mapper;

    @InjectMocks
    private MonitorInvitationService service;

    private final MockMultipartFile cni = new MockMultipartFile("cni", "cni.png", "image/png", new byte[]{1});
    private final MockMultipartFile capec = new MockMultipartFile("capec", "capec.png", "image/png", new byte[]{2});
    private User owner;
    private DrivingSchoolRegistry registry;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.cm");
        registry = new DrivingSchoolRegistry();
        registry.setId(UUID.randomUUID());
        registry.setSchoolName("Auto-école Test");
    }

    private static AddMonitorRequest request(String email) {
        return new AddMonitorRequest("Paul", "Mbarga", email, "+237690000000", Gender.MALE, "Camerounaise", "Douala",
                Date.valueOf("1990-01-01"));
    }

    @Test
    void addMonitor_CreatesAccountLinkDocumentsAndInvitation() {
        when(schoolAccess.requireOwnerOfCurrentSchool()).thenReturn(owner);
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.of(registry));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(monitorsRepository.save(any(Monitor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(currentSchoolProvider.get()).thenReturn(new DrivingSchool());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        service.addMonitor(request(" Paul.Mbarga@Test.cm "), cni, "AB123", capec, null);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        assertEquals("paul.mbarga@test.cm", user.getValue().getEmail());
        assertEquals(Role.MONITOR, user.getValue().getRoles());
        assertEquals(ProfileStatus.REGISTERED, user.getValue().getProfileStatus());
        assertNull(user.getValue().getPrivacyPolicyAcceptedAt(), "le consentement est donné par le moniteur lui-même");

        ArgumentCaptor<SchoolJoinRequest> link = ArgumentCaptor.forClass(SchoolJoinRequest.class);
        verify(joinRequestRepository).save(link.capture());
        assertEquals(JoinStatus.APPROVED, link.getValue().getJoinStatus());
        assertEquals(registry.getId(), link.getValue().getDrivingSchoolId());

        verify(documentService).uploadVerified(user.getValue(), DocumentType.CNI, cni, "AB123", "owner@test.cm");
        verify(documentService).uploadVerified(user.getValue(), DocumentType.CAPEC, capec, null, "owner@test.cm");

        ArgumentCaptor<PasswordResetToken> token = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(token.capture());
        assertTrue(token.getValue().getToken().length() >= 43, "jeton de 256 bits");
        assertTrue(token.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusHours(71)));
        verify(emailService).sendMonitorInvitationEmail("paul.mbarga@test.cm", "Paul", "Auto-école Test",
                token.getValue().getToken());
    }

    @Test
    void addMonitor_ByNonOwner_IsForbidden() {
        when(schoolAccess.requireOwnerOfCurrentSchool()).thenThrow(new AccessDeniedException("non"));
        assertThrows(AccessDeniedException.class, () -> service.addMonitor(request("a@test.cm"), cni, null, capec, null));
        verifyNoInteractions(userRepository, documentService, emailService);
    }

    @Test
    void addMonitor_ExistingEmail_IsConflict() {
        when(schoolAccess.requireOwnerOfCurrentSchool()).thenReturn(owner);
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.of(registry));
        when(userRepository.existsByEmail("a@test.cm")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.addMonitor(request("a@test.cm"), cni, null, capec, null));
        verify(userRepository, never()).save(any());
    }

    private PasswordResetToken token(boolean used, LocalDateTime expiresAt) {
        User user = new User();
        user.setProfileStatus(ProfileStatus.REGISTERED);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken("tok");
        token.setUsed(used);
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void acceptInvitation_SetsPasswordConsentAndActivates() {
        PasswordResetToken token = token(false, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken("tok")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Password123")).thenReturn("hash");

        service.acceptInvitation(new AcceptInvitationRequest("tok", "Password123", true));

        User user = token.getUser();
        assertEquals("hash", user.getPassword());
        assertEquals(ProfileStatus.ACTIVE, user.getProfileStatus());
        assertEquals(PrivacyPolicy.CURRENT_VERSION, user.getPrivacyPolicyVersion());
        assertNotNull(user.getPrivacyPolicyAcceptedAt());
        assertTrue(token.isUsed());
    }

    @Test
    void acceptInvitation_InvalidUsedOrExpiredToken_IsRefused() {
        when(tokenRepository.findByToken("inconnu")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
                () -> service.acceptInvitation(new AcceptInvitationRequest("inconnu", "Password123", true)));

        when(tokenRepository.findByToken("tok")).thenReturn(Optional.of(token(true, LocalDateTime.now().plusHours(1))));
        assertTrue(assertThrows(BadRequestException.class,
                () -> service.acceptInvitation(new AcceptInvitationRequest("tok", "Password123", true)))
                .getMessage().contains("déjà été utilisé"));

        when(tokenRepository.findByToken("tok")).thenReturn(Optional.of(token(false, LocalDateTime.now().minusMinutes(1))));
        assertTrue(assertThrows(BadRequestException.class,
                () -> service.acceptInvitation(new AcceptInvitationRequest("tok", "Password123", true)))
                .getMessage().contains("expiré"));

        assertThrows(BadRequestException.class,
                () -> service.acceptInvitation(new AcceptInvitationRequest("tok", "Password123", false)));
    }
}
