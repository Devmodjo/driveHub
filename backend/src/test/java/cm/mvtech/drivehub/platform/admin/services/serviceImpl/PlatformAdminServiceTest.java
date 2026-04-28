package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.AdminPrincipal;
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

    @Mock
    private PlatformAdminRepository adminRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PlatformAdminMapper adminMapper;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private PlatformAdminService platformAdminService;

    private PlatformAdmin platformAdmin;

    @BeforeEach
    void setUp() {
        platformAdmin = new PlatformAdmin();
        platformAdmin.setId(UUID.randomUUID());
        platformAdmin.setName("Platform Admin");
        platformAdmin.setEmail("admin@platform.com");
        platformAdmin.setPassword("encodedPassword");
        platformAdmin.setRole(AdminRole.REVIEWER);
        platformAdmin.setAdminStatus(AdminStatus.ACTIVE);
    }

    @Test
    void adminerLogin_Success() {
        PlatformAdminLoginRequest loginRequest = new PlatformAdminLoginRequest("admin@platform.com", "password");
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generatePlatformAdminToken(any(PlatformAdmin.class))).thenReturn("adminToken");

        PlatformAdminAuthResponse response = platformAdminService.adminerLogin(loginRequest);

        assertNotNull(response);
        assertEquals("adminToken", response.token());
        assertEquals(AdminRole.REVIEWER, response.role());
        assertEquals(AdminStatus.ACTIVE, response.status());
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtService, times(1)).generatePlatformAdminToken(any(PlatformAdmin.class));
    }

    @Test
    void adminerLogin_UserNotFound() {
        PlatformAdminLoginRequest loginRequest = new PlatformAdminLoginRequest("nonexistent@platform.com", "password");
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void adminerLogin_InvalidPassword() {
        PlatformAdminLoginRequest loginRequest = new PlatformAdminLoginRequest("admin@platform.com", "wrongPassword");
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerLogin_AccountNotActive() {
        platformAdmin.setAdminStatus(AdminStatus.PENDING);
        PlatformAdminLoginRequest loginRequest = new PlatformAdminLoginRequest("admin@platform.com", "password");
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.of(platformAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> platformAdminService.adminerLogin(loginRequest));
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    void adminerRegistry_Success() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "New Admin", "new@platform.com", AdminRole.REVIEWER, "password", "123456789", "Residence"
        );
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(PlatformAdmin.class))).thenReturn(platformAdmin);

        platformAdminService.adminerRegistry(createRequest);

        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(adminRepository, times(1)).save(any(PlatformAdmin.class));
    }

    @Test
    void adminerRegistry_CannotCreateRoot() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "New Root", "root@platform.com",  AdminRole.ROOT, "password", "123456789", "Residence"
        );

        assertThrows(IllegalArgumentException.class, () -> platformAdminService.adminerRegistry(createRequest));
        verifyNoInteractions(adminRepository, passwordEncoder);
    }

    @Test
    void adminerRegistry_EmailAlreadyUsed() {
        PlatformAdminCreateRequest createRequest = new PlatformAdminCreateRequest(
                "Existing Admin", "admin@platform.com", AdminRole.SUPER_ADMIN, "password","123456789", "Residence"
        );
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.of(platformAdmin));

        assertThrows(IllegalArgumentException.class, () -> platformAdminService.adminerRegistry(createRequest));
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(adminRepository, times(0)).save(any(PlatformAdmin.class));
    }

    @Test
    void pendingAdminerRequest_Success() {
        PlatformAdmin pendingAdmin = new PlatformAdmin();
        pendingAdmin.setAdminStatus(AdminStatus.PENDING);
        when(adminRepository.findByAdminStatus(AdminStatus.PENDING)).thenReturn(Arrays.asList(pendingAdmin));
        when(adminMapper.toResponse(any(PlatformAdmin.class))).thenReturn(new PlatformAdminResponse(
                UUID.randomUUID(), "Pending Admin", "pending@platform.com", AdminRole.SUPER_ADMIN, AdminStatus.PENDING));

        List<PlatformAdminResponse> result = platformAdminService.pendingAdminerRequest();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(adminRepository, times(1)).findByAdminStatus(AdminStatus.PENDING);
        verify(adminMapper, times(1)).toResponse(any(PlatformAdmin.class));
    }

    @Test
    void getCurrentAdmin_Success() {
        AdminPrincipal adminPrincipal = AdminPrincipal.build(new PlatformAdmin());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminPrincipal);

        PlatformAdminResponse response = platformAdminService.getCurrentAdmin(authentication);

        assertNotNull(response);
        assertEquals(platformAdmin.getId(), response.id());
        assertEquals(platformAdmin.getName(), response.name());
        assertEquals(platformAdmin.getEmail(), response.email());
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    void getCurrentAdmin_NotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> platformAdminService.getCurrentAdmin(authentication));
        verify(authentication, times(1)).isAuthenticated();
    }

    @Test
    void activateAdmin_Success() {
        UUID adminId = platformAdmin.getId();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.of(platformAdmin));
        when(adminRepository.save(any(PlatformAdmin.class))).thenReturn(platformAdmin);

        platformAdminService.activateAdmin(adminId);

        assertEquals(AdminStatus.ACTIVE, platformAdmin.getAdminStatus());
        verify(adminRepository, times(1)).findById(adminId);
        verify(adminRepository, times(1)).save(platformAdmin);
    }

    @Test
    void activateAdmin_NotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> platformAdminService.activateAdmin(adminId));
        verify(adminRepository, times(1)).findById(adminId);
        verify(adminRepository, times(0)).save(any(PlatformAdmin.class));
    }
}
