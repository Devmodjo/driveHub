package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.application.dto.ForgotPasswordRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.ResetPasswordRequest;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.AdminEmailVerificationToken;
import cm.mvtech.drivehub.platform.admin.models.AdminPasswordResetToken;
import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.models.dto.*;
import cm.mvtech.drivehub.platform.admin.models.mappers.PlatformAdminMapper;
import cm.mvtech.drivehub.platform.admin.repositories.AdminEmailVerificationTokenRepository;
import cm.mvtech.drivehub.platform.admin.repositories.AdminPasswordResetTokenRepository;
import cm.mvtech.drivehub.platform.admin.repositories.PlatformAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAdminServiceTest {

    @Mock private PlatformAdminRepository adminRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PlatformAdminMapper adminMapper;
    @Mock private Authentication authentication;
    @Mock private EmailService emailService;
    @Mock private AdminEmailVerificationTokenRepository adminEmailTokenRepository;
    @Mock private AdminPasswordResetTokenRepository adminPasswordResetRepository;

    @InjectMocks
    private PlatformAdminService platformAdminService;

    private PlatformAdmin platformAdmin;
    private PlatformAdminResponse platformAdminResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                platformAdminService, "frontendUrl", "http://localhost:4200");

        platformAdmin = new PlatformAdmin();
        platformAdmin.setId(UUID.randomUUID());
        platformAdmin.setName("Platform Admin");
        platformAdmin.setEmail("admin@platform.com");
        platformAdmin.setPassword("encodedPassword");
        platformAdmin.setRole(AdminRole.REVIEWER);
        platformAdmin.setAdminStatus(AdminStatus.ACTIVE);
        platformAdmin.setResidence("Yaoundé");
        platformAdmin.setPhoneNumber("677000000");
        platformAdmin.setReason("Je souhaite rejoindre l'équipe DriveHub");

        platformAdminResponse = new PlatformAdminResponse(
                platformAdmin.getId(),
                platformAdmin.getName(),
                platformAdmin.getEmail(),
                platformAdmin.getRole(),
                platformAdmin.getAdminStatus(),
                platformAdmin.getResidence(),
                platformAdmin.getPhoneNumber(),
                LocalDateTime.now()
        );
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Test
    void adminerLogin_Success() {
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generatePlatformAdminToken(any(PlatformAdmin.class)))
                .thenReturn("adminToken");

        PlatformAdminAuthResponse response = platformAdminService.adminerLogin(
                new PlatformAdminLoginRequest("admin@platform.com", "password"));

        assertNotNull(response);
        assertEquals("adminToken", response.token());
        assertEquals(AdminRole.REVIEWER, response.role());
        assertEquals(AdminStatus.ACTIVE, response.status());
        verify(adminRepository).findByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtService).generatePlatformAdminToken(any(PlatformAdmin.class));
    }

    @Test
    void adminerLogin_UserNotFound() {
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> platformAdminService.adminerLogin(
                        new PlatformAdminLoginRequest("none@test.cm", "pass")));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void adminerLogin_InvalidPassword() {
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(
                        new PlatformAdminLoginRequest("admin@platform.com", "wrong")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerLogin_EmailPending_ShouldBlock() {
        platformAdmin.setAdminStatus(AdminStatus.EMAIL_PENDING);
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(
                        new PlatformAdminLoginRequest("admin@platform.com", "pass")));

        assertTrue(ex.getMessage().contains("email"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerLogin_PendingAccount_ShouldBlock() {
        platformAdmin.setAdminStatus(AdminStatus.PENDING);
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(
                        new PlatformAdminLoginRequest("admin@platform.com", "pass")));

        assertTrue(ex.getMessage().contains("ROOT"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerLogin_SuspendedAccount_ShouldBlock() {
        platformAdmin.setAdminStatus(AdminStatus.SUSPENDED);
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(
                        new PlatformAdminLoginRequest("admin@platform.com", "pass")));
        verifyNoInteractions(jwtService);
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────

    @Test
    void adminerRegistry_Success() {
        PlatformAdminCreateRequest request = new PlatformAdminCreateRequest(
                "New Admin", "new@platform.com", AdminRole.REVIEWER,
                "password", "Yaoundé", "677000000",
                "Je veux contribuer à DriveHub"
        );
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(PlatformAdmin.class))).thenReturn(platformAdmin);
        when(adminEmailTokenRepository.save(any())).thenReturn(new AdminEmailVerificationToken());
        doNothing().when(emailService).sendAdminEmailVerification(
                anyString(), anyString(), anyString(), anyString(), anyString());

        platformAdminService.adminerRegistry(request);

        verify(adminRepository).findByEmail(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(adminRepository).save(any(PlatformAdmin.class));
        verify(emailService).sendAdminEmailVerification(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void adminerRegistry_CannotCreateRoot() {
        PlatformAdminCreateRequest request = new PlatformAdminCreateRequest(
                "Root", "root@platform.com", AdminRole.ROOT,
                "password", "Yaoundé", "677000000", "motif"
        );

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminerRegistry(request));
        verifyNoInteractions(adminRepository, passwordEncoder);
    }

    @Test
    void adminerRegistry_EmailAlreadyUsed() {
        PlatformAdminCreateRequest request = new PlatformAdminCreateRequest(
                "Existing", "admin@platform.com", AdminRole.SUPER_ADMIN,
                "password", "Yaoundé", "677000000", "motif"
        );
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminerRegistry(request));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── PENDING ──────────────────────────────────────────────────────────────

    @Test
    void pendingAdminerRequest_Success() {
        PlatformAdmin pendingAdmin = new PlatformAdmin();
        pendingAdmin.setAdminStatus(AdminStatus.PENDING);

        PlatformAdminResponse pendingResponse = new PlatformAdminResponse(
                UUID.randomUUID(), "Pending", "pending@platform.com",
                AdminRole.SUPER_ADMIN, AdminStatus.PENDING, "SYSTEM", "40404",LocalDateTime.now()
        );

        when(adminRepository.findByAdminStatus(AdminStatus.PENDING))
                .thenReturn(List.of(pendingAdmin));
        when(adminMapper.toResponse(any(PlatformAdmin.class)))
                .thenReturn(pendingResponse);

        List<PlatformAdminResponse> result =
                platformAdminService.pendingAdminerRequest();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AdminStatus.PENDING, result.get(0).adminStatus());
    }

    @Test
    void pendingAdminerRequest_EmptyList() {
        when(adminRepository.findByAdminStatus(AdminStatus.PENDING))
                .thenReturn(List.of());

        assertTrue(platformAdminService.pendingAdminerRequest().isEmpty());
    }

    // ─── ACTIVATE ADMIN ───────────────────────────────────────────────────────

    @Test
    void activateAdmin_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminRepository.save(any(PlatformAdmin.class)))
                .thenReturn(platformAdmin);
        doNothing().when(emailService).sendAdminWelcomeMail(
                anyString(), anyString(), anyString(), anyString(), any());

        platformAdminService.activateAdmin(adminId);

        assertEquals(AdminStatus.ACTIVE, platformAdmin.getAdminStatus());
        verify(adminRepository).findById(adminId);
        verify(adminRepository).save(platformAdmin);
        verify(emailService).sendAdminWelcomeMail(
                anyString(), anyString(), eq("ACTIVE"), anyString(), any());
    }

    @Test
    void activateAdmin_NotFound() {
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> platformAdminService.activateAdmin(UUID.randomUUID()));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── VERIFY EMAIL ─────────────────────────────────────────────────────────

    @Test
    void verifyAdminEmail_Success() {
        platformAdmin.setAdminStatus(AdminStatus.EMAIL_PENDING);

        AdminEmailVerificationToken verificationToken =
                new AdminEmailVerificationToken();
        verificationToken.setAdmin(platformAdmin);
        verificationToken.setToken("valid-token");
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);

        PlatformAdmin rootAdmin = new PlatformAdmin();
        rootAdmin.setEmail("root@drivehub.cm");
        rootAdmin.setName("ROOT");

        when(adminEmailTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(verificationToken));
        when(adminRepository.save(any(PlatformAdmin.class)))
                .thenReturn(platformAdmin);
        when(adminRepository.findByRole(AdminRole.ROOT))
                .thenReturn(List.of(rootAdmin));
        doNothing().when(emailService).sendAdminWelcomeMail(
                anyString(), anyString(), anyString(), anyString(), any());
        doNothing().when(emailService).sendNewAdminRegistrationNotification(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(UUID.class));

        platformAdminService.verifyAdminEmail("valid-token");

        assertEquals(AdminStatus.PENDING, platformAdmin.getAdminStatus());
        verify(adminRepository).save(platformAdmin);
        verify(emailService).sendAdminWelcomeMail(
                anyString(), anyString(), eq("PENDING"), anyString(), any());
        verify(emailService).sendNewAdminRegistrationNotification(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(UUID.class));
        assertTrue(verificationToken.isUsed());
    }

    @Test
    void verifyAdminEmail_InvalidToken() {
        when(adminEmailTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.verifyAdminEmail("bad-token"));
    }

    @Test
    void verifyAdminEmail_ExpiredToken() {
        AdminEmailVerificationToken expiredToken = new AdminEmailVerificationToken();
        expiredToken.setAdmin(platformAdmin);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setUsed(false);

        when(adminEmailTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.verifyAdminEmail("expired-token"));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    @Test
    void verifyAdminEmail_AlreadyUsedToken() {
        AdminEmailVerificationToken usedToken = new AdminEmailVerificationToken();
        usedToken.setAdmin(platformAdmin);
        usedToken.setToken("used-token");
        usedToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        usedToken.setUsed(true);

        when(adminEmailTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(usedToken));

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.verifyAdminEmail("used-token"));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────

    @Test
    void adminForgotPassword_SendsEmail_WhenAdminExists() {
        when(adminRepository.findByEmail("admin@platform.com"))
                .thenReturn(Optional.of(platformAdmin));
        when(adminPasswordResetRepository.save(any()))
                .thenReturn(new AdminPasswordResetToken());
        doNothing().when(emailService).sendPasswordResetEmail(
                anyString(), anyString(), anyString());

        platformAdminService.adminForgotPassword(
                new ForgotPasswordRequest("admin@platform.com"));

        verify(adminPasswordResetRepository).deleteAllByAdminId(platformAdmin.getId());
        verify(adminPasswordResetRepository).save(any(AdminPasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(
                anyString(), anyString(), anyString());
    }

    @Test
    void adminForgotPassword_DoesNothing_WhenAdminNotFound() {
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        platformAdminService.adminForgotPassword(
                new ForgotPasswordRequest("unknown@test.cm"));

        verifyNoInteractions(adminPasswordResetRepository, emailService);
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────

    @Test
    void adminResetPassword_Success() {
        AdminPasswordResetToken resetToken = new AdminPasswordResetToken();
        resetToken.setAdmin(platformAdmin);
        resetToken.setToken("valid-reset-token");
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(adminPasswordResetRepository.findByToken("valid-reset-token"))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(adminRepository.save(any(PlatformAdmin.class))).thenReturn(platformAdmin);

        platformAdminService.adminResetPassword(
                new ResetPasswordRequest("valid-reset-token", "newPassword123"));

        assertEquals("newEncodedPassword", platformAdmin.getPassword());
        assertTrue(resetToken.isUsed());
        verify(adminRepository).save(platformAdmin);
    }

    @Test
    void adminResetPassword_InvalidToken() {
        when(adminPasswordResetRepository.findByToken(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminResetPassword(
                        new ResetPasswordRequest("bad-token", "newPass")));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    @Test
    void adminResetPassword_ExpiredToken() {
        AdminPasswordResetToken expiredToken = new AdminPasswordResetToken();
        expiredToken.setAdmin(platformAdmin);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setUsed(false);

        when(adminPasswordResetRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminResetPassword(
                        new ResetPasswordRequest("expired-token", "newPass")));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── DEACTIVATE ADMIN ─────────────────────────────────────────────────────

    @Test
    void deactivateAdmin_Success() {
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminRepository.save(any(PlatformAdmin.class)))
                .thenReturn(platformAdmin);

        platformAdminService.deactivateAdmin(platformAdmin.getId());

        assertEquals(AdminStatus.SUSPENDED, platformAdmin.getAdminStatus());
        verify(adminRepository).save(platformAdmin);
    }

    @Test
    void deactivateAdmin_CannotDeactivateRoot() {
        platformAdmin.setRole(AdminRole.ROOT);
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.deactivateAdmin(platformAdmin.getId()));
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    @Test
    void deactivateAdmin_NotFound() {
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.deactivateAdmin(UUID.randomUUID()));
    }

    // ─── DELETE ADMIN ─────────────────────────────────────────────────────────

    @Test
    void deleteAdmin_Success() {
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        doNothing().when(adminRepository).delete(any(PlatformAdmin.class));

        platformAdminService.deleteAdmin(platformAdmin.getId());

        verify(adminRepository).delete(platformAdmin);
    }

    @Test
    void deleteAdmin_CannotDeleteRoot() {
        platformAdmin.setRole(AdminRole.ROOT);
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.deleteAdmin(platformAdmin.getId()));
        verify(adminRepository, never()).delete(any(PlatformAdmin.class));
    }

    @Test
    void deleteAdmin_NotFound() {
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.deleteAdmin(UUID.randomUUID()));
    }

    // ─── GET ADMIN BY ID ──────────────────────────────────────────────────────

    @Test
    void getAdminById_Success() {
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminMapper.toResponse(any(PlatformAdmin.class)))
                .thenReturn(platformAdminResponse);

        PlatformAdminResponse result =
                platformAdminService.getAdminById(platformAdmin.getId());

        assertNotNull(result);
        assertEquals(platformAdmin.getName(), result.name());
        assertEquals(platformAdmin.getEmail(), result.email());
    }

    @Test
    void getAdminById_NotFound() {
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.getAdminById(UUID.randomUUID()));
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    @Test
    void getAdminStats_Success() {
        when(adminRepository.count()).thenReturn(10L);
        when(adminRepository.countByAdminStatus(AdminStatus.PENDING)).thenReturn(2L);
        when(adminRepository.countByAdminStatus(AdminStatus.ACTIVE)).thenReturn(7L);
        when(adminRepository.countByAdminStatus(AdminStatus.SUSPENDED)).thenReturn(1L);
        when(adminRepository.countByAdminStatus(AdminStatus.DISABLED)).thenReturn(0L);

        AdminStatsResponse stats = platformAdminService.getAdminStats();

        assertEquals(10L, stats.totalAdmins());
        assertEquals(2L, stats.pendingAdmins());
        assertEquals(7L, stats.activeAdmins());
        assertEquals(1L, stats.inactiveAdmins());
    }
}