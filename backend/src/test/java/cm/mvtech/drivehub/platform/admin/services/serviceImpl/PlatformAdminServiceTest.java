package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;

import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminCreateRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminResponse;
import cm.mvtech.drivehub.platform.admin.models.mappers.PlatformAdminMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @InjectMocks
    private PlatformAdminService platformAdminService;

    private PlatformAdmin platformAdmin;
    private PlatformAdminResponse platformAdminResponse;

    @BeforeEach
    void setUp() {
        platformAdmin = new PlatformAdmin();
        platformAdmin.setId(UUID.randomUUID());
        platformAdmin.setName("Platform Admin");
        platformAdmin.setEmail("admin@platform.com");
        platformAdmin.setPassword("encodedPassword");
        platformAdmin.setRole(AdminRole.REVIEWER);
        platformAdmin.setAdminStatus(AdminStatus.ACTIVE);

        // ← Helper partagé — tous les tests utilisent ceci
        platformAdminResponse = new PlatformAdminResponse(
                platformAdmin.getId(),
                platformAdmin.getName(),
                platformAdmin.getEmail(),
                platformAdmin.getRole(),
                platformAdmin.getAdminStatus(),
                LocalDateTime.now()   // ← champ manquant ajouté
        );
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Test
    void adminerLogin_Success() {
        PlatformAdminLoginRequest loginRequest =
                new PlatformAdminLoginRequest("admin@platform.com", "password");
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generatePlatformAdminToken(any(PlatformAdmin.class)))
                .thenReturn("adminToken");

        PlatformAdminAuthResponse response =
                platformAdminService.adminerLogin(loginRequest);

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
        PlatformAdminLoginRequest loginRequest =
                new PlatformAdminLoginRequest("nonexistent@platform.com", "password");
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository).findByEmail(anyString());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void adminerLogin_InvalidPassword() {
        PlatformAdminLoginRequest loginRequest =
                new PlatformAdminLoginRequest("admin@platform.com", "wrongPassword");
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository).findByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerLogin_AccountNotActive() {
        platformAdmin.setAdminStatus(AdminStatus.PENDING);
        PlatformAdminLoginRequest loginRequest =
                new PlatformAdminLoginRequest("admin@platform.com", "password");
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository).findByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────

    @Test
    void adminerRegistry_Success() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "New Admin", "new@platform.com",
                AdminRole.REVIEWER, "password", "Residence", "123456789"
        );
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(PlatformAdmin.class))).thenReturn(platformAdmin);

        platformAdminService.adminerRegistry(createRequest);

        verify(adminRepository).findByEmail(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(adminRepository).save(any(PlatformAdmin.class));
    }

    @Test
    void adminerRegistry_CannotCreateRoot() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "New Root", "root@platform.com",
                AdminRole.ROOT, "password", "Residence", "123456789"
        );

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminerRegistry(createRequest));
        verifyNoInteractions(adminRepository, passwordEncoder);
    }

    @Test
    void adminerRegistry_EmailAlreadyUsed() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "Existing Admin", "admin@platform.com",
                AdminRole.SUPER_ADMIN, "password", "Residence", "123456789"
        );
        when(adminRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.adminerRegistry(createRequest));
        verify(adminRepository).findByEmail(anyString());
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── PENDING ──────────────────────────────────────────────────────────────

    @Test
    void pendingAdminerRequest_Success() {
        PlatformAdmin pendingAdmin = new PlatformAdmin();
        pendingAdmin.setAdminStatus(AdminStatus.PENDING);

        // ← PlatformAdminResponse avec 6 champs dont createdAt
        PlatformAdminResponse pendingResponse = new PlatformAdminResponse(
                UUID.randomUUID(),
                "Pending Admin",
                "pending@platform.com",
                AdminRole.SUPER_ADMIN,
                AdminStatus.PENDING,
                LocalDateTime.now()   // ← champ manquant ajouté
        );

        when(adminRepository.findByAdminStatus(AdminStatus.PENDING))
                .thenReturn(Arrays.asList(pendingAdmin));
        when(adminMapper.toResponse(any(PlatformAdmin.class)))
                .thenReturn(pendingResponse);

        List<PlatformAdminResponse> result =
                platformAdminService.pendingAdminerRequest();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(AdminStatus.PENDING, result.get(0).adminStatus());
        verify(adminRepository).findByAdminStatus(AdminStatus.PENDING);
        verify(adminMapper).toResponse(any(PlatformAdmin.class));
    }

    @Test
    void pendingAdminerRequest_EmptyList() {
        when(adminRepository.findByAdminStatus(AdminStatus.PENDING))
                .thenReturn(List.of());

        List<PlatformAdminResponse> result =
                platformAdminService.pendingAdminerRequest();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adminRepository).findByAdminStatus(AdminStatus.PENDING);
    }

    // ─── GET CURRENT ADMIN ────────────────────────────────────────────────────

    @Test
    void getCurrentAdmin_NotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.getCurrentAdmin(authentication));
        verify(authentication).isAuthenticated();
    }

    // ─── ACTIVATE ADMIN ───────────────────────────────────────────────────────

    @Test
    void activateAdmin_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminRepository.save(any(PlatformAdmin.class)))
                .thenReturn(platformAdmin);

        platformAdminService.activateAdmin(adminId);

        assertEquals(AdminStatus.ACTIVE, platformAdmin.getAdminStatus());
        verify(adminRepository).findById(adminId);
        verify(adminRepository).save(platformAdmin);
    }

    @Test
    void activateAdmin_NotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> platformAdminService.activateAdmin(adminId));
        verify(adminRepository).findById(adminId);
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── DEACTIVATE ADMIN ─────────────────────────────────────────────────────

    @Test
    void deactivateAdmin_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminRepository.save(any(PlatformAdmin.class)))
                .thenReturn(platformAdmin);

        platformAdminService.deactivateAdmin(adminId);

        assertEquals(AdminStatus.SUSPENDED, platformAdmin.getAdminStatus());
        verify(adminRepository).findById(adminId);
        verify(adminRepository).save(platformAdmin);
    }

    @Test
    void deactivateAdmin_CannotDeactivateRoot() {
        platformAdmin.setRole(AdminRole.ROOT);
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.deactivateAdmin(adminId));
        verify(adminRepository).findById(adminId);
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    @Test
    void deactivateAdmin_NotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.deactivateAdmin(adminId));
        verify(adminRepository).findById(adminId);
        verify(adminRepository, never()).save(any(PlatformAdmin.class));
    }

    // ─── DELETE ADMIN ─────────────────────────────────────────────────────────

    @Test
    void deleteAdmin_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        doNothing().when(adminRepository).delete(any(PlatformAdmin.class));

        platformAdminService.deleteAdmin(adminId);

        verify(adminRepository).findById(adminId);
        verify(adminRepository).delete(platformAdmin);
    }

    @Test
    void deleteAdmin_CannotDeleteRoot() {
        platformAdmin.setRole(AdminRole.ROOT);
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));

        assertThrows(AccessDeniedException.class,
                () -> platformAdminService.deleteAdmin(adminId));
        verify(adminRepository).findById(adminId);
        verify(adminRepository, never()).delete(any(PlatformAdmin.class));
    }

    @Test
    void deleteAdmin_NotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.deleteAdmin(adminId));
        verify(adminRepository).findById(adminId);
        verify(adminRepository, never()).delete(any(PlatformAdmin.class));
    }

    // ─── GET ADMIN BY ID ──────────────────────────────────────────────────────

    @Test
    void getAdminById_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(platformAdmin));
        when(adminMapper.toResponse(any(PlatformAdmin.class)))
                .thenReturn(platformAdminResponse);

        PlatformAdminResponse result = platformAdminService.getAdminById(adminId);

        assertNotNull(result);
        assertEquals(platformAdmin.getName(), result.name());
        assertEquals(platformAdmin.getEmail(), result.email());
        verify(adminRepository).findById(adminId);
        verify(adminMapper).toResponse(platformAdmin);
    }

    @Test
    void getAdminById_NotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> platformAdminService.getAdminById(adminId));
        verify(adminRepository).findById(adminId);
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    @Test
    void getAdminStats_Success() {
        when(adminRepository.count()).thenReturn(10L);
        when(adminRepository.countByAdminStatus(AdminStatus.PENDING)).thenReturn(2L);
        when(adminRepository.countByAdminStatus(AdminStatus.ACTIVE)).thenReturn(7L);
        when(adminRepository.countByAdminStatus(AdminStatus.SUSPENDED)).thenReturn(1L);
        when(adminRepository.countByAdminStatus(AdminStatus.DISABLED)).thenReturn(0L);

        var stats = platformAdminService.getAdminStats();

        assertNotNull(stats);
        assertEquals(10L, stats.totalAdmins());
        assertEquals(2L, stats.pendingAdmins());
        assertEquals(7L, stats.activeAdmins());
        assertEquals(1L, stats.inactiveAdmins());
        verify(adminRepository).count();
        verify(adminRepository).countByAdminStatus(AdminStatus.PENDING);
        verify(adminRepository).countByAdminStatus(AdminStatus.ACTIVE);
    }
}