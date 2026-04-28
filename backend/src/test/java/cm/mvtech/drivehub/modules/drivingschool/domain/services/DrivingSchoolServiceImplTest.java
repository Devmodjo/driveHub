package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.core.domain.service.TenantProvisioningService;
import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolMapper;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrivingSchoolServiceImplTest {

    @Mock
    private DrivingSchoolRepository drivingSchoolRepository;
    @Mock
    private TenantProvisioningService tenantProvisioningService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    @Mock
    private DrivingSchoolMapper mapper;

    @InjectMocks
    private DrivingSchoolServiceImpl drivingSchoolService;

    private User adminUser;
    private UserPrincipal userPrincipal;
    private DrivingSchoolRequestDto drivingSchoolRequestDto;
    private DrivingSchoolRegistry drivingSchoolRegistry;
    private DrivingSchool drivingSchool;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setFirstname("Admin");
        adminUser.setLastname("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Role.MONITOR);
        adminUser.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);

        userPrincipal = UserPrincipal.build(adminUser);

        drivingSchoolRequestDto = new DrivingSchoolRequestDto(
                "Test School", "123 Main St", "1234567890", "test@example.com",
                "Description", "www.test.com", "1234567890", "Country", "City"
        );

        drivingSchoolRegistry = new DrivingSchoolRegistry();
        drivingSchoolRegistry.setId(UUID.randomUUID());
        drivingSchoolRegistry.setSchoolName("Test School");
        drivingSchoolRegistry.setSchemaName("test_school");
        drivingSchoolRegistry.setAdmin(adminUser);
        drivingSchoolRegistry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);

        drivingSchool = new DrivingSchool();
        drivingSchool.setId(UUID.randomUUID());
        drivingSchool.setName("Test School");
        drivingSchool.setAddress("123 Main St");
        drivingSchool.setPhoneNumber("1234567890");
        drivingSchool.setDescription("Description");
        drivingSchool.setCreatedAt(LocalDate.now());
    }

    @Test
    void createSchool_Success() throws IllegalAccessException {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(adminUser));
        when(drivingSchoolRegistryRepository.save(any(DrivingSchoolRegistry.class))).thenReturn(drivingSchoolRegistry);

        drivingSchoolService.createSchool(drivingSchoolRequestDto, userPrincipal);

        verify(userRepository, times(1)).findById(adminUser.getId());
        verify(drivingSchoolRegistryRepository, times(1)).save(any(DrivingSchoolRegistry.class));
    }

    @Test
    void createSchool_UserNotFound() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> drivingSchoolService.createSchool(drivingSchoolRequestDto, userPrincipal));
        verify(userRepository, times(1)).findById(adminUser.getId());
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    @Test
    void createSchool_EmailNotVerified() {
        adminUser.setProfileStatus(ProfileStatus.REGISTERED);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalAccessException.class, () -> drivingSchoolService.createSchool(drivingSchoolRequestDto, userPrincipal));
        verify(userRepository, times(1)).findById(adminUser.getId());
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    @Test
    void createSchool_UserNotMonitor() {
        adminUser.setRoles(Role.STUDENT);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () -> drivingSchoolService.createSchool(drivingSchoolRequestDto, userPrincipal));
        verify(userRepository, times(1)).findById(adminUser.getId());
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    @Test
    void retrieveSchool_Success() {
        DrivingSchoolResponseDto responseDto = new DrivingSchoolResponseDto(
                drivingSchool.getId(), drivingSchool.getName(), drivingSchool.getPhoneNumber(),
                drivingSchool.getAddress(), drivingSchool.getDescription(), drivingSchool.getCreatedAt()
        );
        when(drivingSchoolRepository.findAll()).thenReturn(Collections.singletonList(drivingSchool));
        // The current implementation directly constructs the DTO, so no mapper mock is needed for this method.

        List<DrivingSchoolResponseDto> result = drivingSchoolService.retreiveSchool();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(drivingSchool.getName(), result.get(0).name());
        verify(drivingSchoolRepository, times(1)).findAll();
    }

    @Test
    void approveRegistry_Success() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class))).thenReturn(Optional.of(drivingSchoolRegistry));
        when(drivingSchoolRepository.save(any(DrivingSchool.class))).thenReturn(drivingSchool);
        when(drivingSchoolRegistryRepository.save(any(DrivingSchoolRegistry.class))).thenReturn(drivingSchoolRegistry);

        drivingSchoolService.approveRegistry(drivingSchoolRegistry.getId());

        verify(drivingSchoolRegistryRepository, times(1)).findById(drivingSchoolRegistry.getId());
        verify(tenantProvisioningService, times(1)).createTenantSchema(drivingSchoolRegistry.getSchemaName());
        verify(userRepository, times(0)).save(any(User.class)); // User is modified directly
        verify(drivingSchoolRepository, times(1)).save(any(DrivingSchool.class));
        verify(drivingSchoolRegistryRepository, times(1)).save(any(DrivingSchoolRegistry.class));
        assertEquals(ProfileStatus.ACTIVE, adminUser.getProfileStatus());
        assertEquals(DrivingSchoolStatus.APPROVED, drivingSchoolRegistry.getDrivingSchoolStatus());
        assertNull(TenantContext.getTenantId()); // Should be cleared after transaction
    }

    @Test
    void approveRegistry_RegistryNotFound() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> drivingSchoolService.approveRegistry(UUID.randomUUID()));
        verify(drivingSchoolRegistryRepository, times(1)).findById(any(UUID.class));
        verifyNoInteractions(tenantProvisioningService, drivingSchoolRepository);
    }

    @Test
    void approveRegistry_UserNotMonitor() {
        drivingSchoolRegistry.getAdmin().setRoles(Role.STUDENT);
        when(drivingSchoolRegistryRepository.findById(any(UUID.class))).thenReturn(Optional.of(drivingSchoolRegistry));

        assertThrows(AccessDeniedException.class, () -> drivingSchoolService.approveRegistry(drivingSchoolRegistry.getId()));
        verify(drivingSchoolRegistryRepository, times(1)).findById(drivingSchoolRegistry.getId());
        verifyNoInteractions(tenantProvisioningService, drivingSchoolRepository);
    }

    @Test
    void retrievePendingRequest_Success() {
        Monitor monitor = new Monitor();
        monitor.setResidenceCity("Douala");
        monitor.setNationality("Cameroonian");
        monitor.setGender(Gender.MALE);
        adminUser.setMonitors((java.util.Set<Monitor>) Collections.singletonList(monitor));

        DrivingSchoolRegistry pendingRegistry = new DrivingSchoolRegistry();
        pendingRegistry.setId(UUID.randomUUID());
        pendingRegistry.setSchoolName("Pending School");
        pendingRegistry.setCountry("Country");
        pendingRegistry.setCity("City");
        pendingRegistry.setAddress("Address");
        pendingRegistry.setWhatsappNumber("123456789");
        pendingRegistry.setAdmin(adminUser);
        pendingRegistry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);

        when(drivingSchoolRegistryRepository.findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING))
                .thenReturn(Collections.singletonList(pendingRegistry));

        List<DrivingSchoolPendingRequestDTO> result = drivingSchoolService.retreivePendingRequest();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Pending School", result.get(0).schoolName());
        assertEquals("Admin User", result.get(0).monitorName());
        assertEquals("Douala", result.get(0).monitorResidence());
        verify(drivingSchoolRegistryRepository, times(1)).findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING);
    }

    @Test
    void retrievePendingRequest_NoPendingRequests() {
        when(drivingSchoolRegistryRepository.findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING))
                .thenReturn(Collections.emptyList());

        List<DrivingSchoolPendingRequestDTO> result = drivingSchoolService.retreivePendingRequest();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(drivingSchoolRegistryRepository, times(1)).findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING);
    }
}
