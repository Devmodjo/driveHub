package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.core.domain.service.TenantProvisioningService;
import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de DrivingSchoolServiceImpl.
 *
 * Chaque méthode du service est couverte par au moins un scénario nominal et un ou plusieurs
 * scénarios d'erreur. Les dépendances (repositories, TenantProvisioningService, mapper) sont
 * toutes mockées via Mockito afin d'isoler complètement la logique métier testée.
 * Les fixtures communes (User, UserPrincipal, DrivingSchoolRegistry, etc.) sont initialisées
 * dans {@link #setUp()} et réutilisées ou surchargées dans chaque test selon le besoin.
 */
@ExtendWith(MockitoExtension.class)
class DrivingSchoolServiceImplTest {

    @Mock private DrivingSchoolRepository drivingSchoolRepository;
    @Mock private TenantProvisioningService tenantProvisioningService;
    @Mock private UserRepository userRepository;
    @Mock private DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    @Mock private DrivingSchoolMapper mapper;

    @InjectMocks
    private DrivingSchoolServiceImpl service;

    private User monitorUser;
    private UserPrincipal userPrincipal;
    private DrivingSchoolRequestDto requestDto;
    private DrivingSchoolRegistry registry;
    private DrivingSchool drivingSchool;
    private Monitor monitor;

    @BeforeEach
    void setUp() {
        monitorUser = new User();
        monitorUser.setId(UUID.randomUUID());
        monitorUser.setFirstname("Jean");
        monitorUser.setLastname("Dupont");
        monitorUser.setEmail("jean.dupont@example.com");
        monitorUser.setRoles(Role.MONITOR);
        monitorUser.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);

        monitor = new Monitor();
        monitor.setResidenceCity("Douala");
        monitor.setNationality("Cameroonian");
        monitor.setGender(Gender.MALE);
        monitor.setPhoneNumber("699000001");
        monitorUser.setMonitors(Set.of(monitor));

        userPrincipal = UserPrincipal.build(monitorUser);

        requestDto = new DrivingSchoolRequestDto(
                "MV Auto École", "Rue de la Paix", "699000000",
                "mv@autoecole.cm", "Meilleure auto-école",
                "www.mv-autoecole.cm", "699000000", "Cameroun", "Yaoundé"
        );

        registry = new DrivingSchoolRegistry();
        registry.setId(UUID.randomUUID());
        registry.setSchoolName("MV Auto École");
        registry.setSchemaName("mv_auto_ecole");
        registry.setAdmin(monitorUser);
        registry.setAddress("Rue de la Paix");
        registry.setPhoneNumber("699000000");
        registry.setEmail("mv@autoecole.cm");
        registry.setCountry("Cameroun");
        registry.setCity("Yaoundé");
        registry.setWhatsappNumber("699000000");
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        registry.setCreatedAt(LocalDate.now());

        drivingSchool = new DrivingSchool();
        drivingSchool.setId(UUID.randomUUID());
        drivingSchool.setName("MV Auto École");
        drivingSchool.setAddress("Rue de la Paix");
        drivingSchool.setPhoneNumber("699000000");
        drivingSchool.setEmail("mv@autoecole.cm");
        drivingSchool.setCreatedAt(LocalDate.now());
        drivingSchool.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
    }

    // ─── createSchool ──────────────────────────────────────────────────────────

    /**
     * Vérifie qu'un moniteur dont l'email est vérifié peut créer une demande d'enregistrement.
     * Le registre doit être sauvegardé une seule fois avec le statut PENDING.
     */
    @Test
    void createSchool_Success() throws IllegalAccessException {
        when(userRepository.findById(monitorUser.getId())).thenReturn(Optional.of(monitorUser));

        service.createSchool(requestDto, userPrincipal);

        verify(userRepository).findById(monitorUser.getId());
        verify(drivingSchoolRegistryRepository).save(any(DrivingSchoolRegistry.class));
    }

    /**
     * Vérifie qu'une UsernameNotFoundException est levée lorsque l'utilisateur
     * associé au principal est introuvable en base.
     */
    @Test
    void createSchool_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.createSchool(requestDto, userPrincipal));
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    /**
     * Vérifie qu'une IllegalAccessException est levée si l'email de l'utilisateur
     * n'a pas encore été vérifié (statut REGISTERED).
     */
    @Test
    void createSchool_EmailNotVerified_ThrowsIllegalAccessException() {
        monitorUser.setProfileStatus(ProfileStatus.REGISTERED);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(monitorUser));

        assertThrows(IllegalAccessException.class,
                () -> service.createSchool(requestDto, userPrincipal));
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    /**
     * Vérifie qu'une AccessDeniedException est levée si l'utilisateur n'a pas le rôle MONITOR,
     * car seul un moniteur est autorisé à créer une auto-école.
     */
    @Test
    void createSchool_UserNotMonitor_ThrowsAccessDeniedException() {
        monitorUser.setRoles(Role.STUDENT);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(monitorUser));

        assertThrows(AccessDeniedException.class,
                () -> service.createSchool(requestDto, userPrincipal));
        verifyNoInteractions(drivingSchoolRegistryRepository);
    }

    // ─── retreiveSchool ────────────────────────────────────────────────────────

    /**
     * Vérifie que la liste des auto-écoles est correctement construite depuis le repository
     * et que les champs du DTO de réponse correspondent bien aux données de l'entité.
     */
    @Test
    void retreiveSchool_ReturnsMappedDtoList() {
        when(drivingSchoolRepository.findAll()).thenReturn(List.of(drivingSchool));

        List<DrivingSchoolResponseDto> result = service.retreiveSchool();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(drivingSchool.getName(), result.get(0).name());
        verify(drivingSchoolRepository).findAll();
    }

    /**
     * Vérifie qu'une liste vide est retournée lorsqu'aucune auto-école n'existe en base.
     */
    @Test
    void retreiveSchool_WhenNoSchools_ReturnsEmptyList() {
        when(drivingSchoolRepository.findAll()).thenReturn(Collections.emptyList());

        List<DrivingSchoolResponseDto> result = service.retreiveSchool();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── approveRegistry ──────────────────────────────────────────────────────

    /**
     * Vérifie le flux complet d'approbation : création du schéma tenant, activation du moniteur,
     * persistance de l'auto-école dans le bon tenant et mise à jour du statut du registre à APPROVED.
     * Le TenantContext doit être vidé après l'opération.
     */
    @Test
    void approveRegistry_Success() {
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));
        when(drivingSchoolRepository.save(any(DrivingSchool.class))).thenReturn(drivingSchool);
        when(drivingSchoolRegistryRepository.save(any(DrivingSchoolRegistry.class))).thenReturn(registry);

        service.approveRegistry(registry.getId());

        verify(tenantProvisioningService).createTenantSchema(registry.getSchemaName());
        verify(drivingSchoolRepository).save(any(DrivingSchool.class));
        verify(drivingSchoolRegistryRepository).save(registry);
        assertEquals(ProfileStatus.ACTIVE, monitorUser.getProfileStatus());
        assertEquals(DrivingSchoolStatus.APPROVED, registry.getDrivingSchoolStatus());
        assertNull(TenantContext.getTenantId());
    }

    /**
     * Vérifie qu'une IllegalArgumentException est levée si l'identifiant du registre
     * ne correspond à aucune entrée existante.
     */
    @Test
    void approveRegistry_RegistryNotFound_ThrowsIllegalArgumentException() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.approveRegistry(UUID.randomUUID()));
        verifyNoInteractions(tenantProvisioningService, drivingSchoolRepository);
    }

    /**
     * Vérifie qu'une AccessDeniedException est levée si l'administrateur du registre
     * n'est pas un MONITOR, empêchant ainsi l'approbation d'un compte non qualifié.
     */
    @Test
    void approveRegistry_AdminNotMonitor_ThrowsAccessDeniedException() {
        monitorUser.setRoles(Role.STUDENT);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        assertThrows(AccessDeniedException.class,
                () -> service.approveRegistry(registry.getId()));
        verifyNoInteractions(tenantProvisioningService, drivingSchoolRepository);
    }

    // ─── retreivePendingRequest ────────────────────────────────────────────────

    /**
     * Vérifie que les demandes en attente sont correctement récupérées et mappées,
     * incluant les informations du moniteur (résidence, nationalité, genre).
     */
    @Test
    void retreivePendingRequest_ReturnsPendingList() {
        when(drivingSchoolRegistryRepository.findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING))
                .thenReturn(List.of(registry));

        List<DrivingSchoolPendingRequestDTO> result = service.retreivePendingRequest();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MV Auto École", result.get(0).schoolName());
        assertEquals("Jean Dupont", result.get(0).monitorName());
        assertEquals("Douala", result.get(0).monitorResidence());
        assertEquals("Cameroonian", result.get(0).monitorNationality());
        assertEquals(Gender.MALE, result.get(0).gender());
        verify(drivingSchoolRegistryRepository).findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING);
    }

    /**
     * Vérifie qu'une liste vide est retournée lorsqu'aucune demande n'est en attente d'approbation.
     */
    @Test
    void retreivePendingRequest_WhenNoPending_ReturnsEmptyList() {
        when(drivingSchoolRegistryRepository.findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING))
                .thenReturn(Collections.emptyList());

        List<DrivingSchoolPendingRequestDTO> result = service.retreivePendingRequest();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── rejectRegistry ───────────────────────────────────────────────────────

    /**
     * Vérifie qu'un registre en statut PENDING peut être rejeté et que le statut
     * est correctement mis à jour à REJECTED puis persisté.
     */
    @Test
    void rejectRegistry_Success() {
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        service.rejectRegistry(registry.getId());

        assertEquals(DrivingSchoolStatus.REJECTED, registry.getDrivingSchoolStatus());
        verify(drivingSchoolRegistryRepository).save(registry);
    }

    /**
     * Vérifie qu'une IllegalArgumentException est levée si l'UUID du registre à rejeter
     * est introuvable en base.
     */
    @Test
    void rejectRegistry_NotFound_ThrowsIllegalArgumentException() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.rejectRegistry(UUID.randomUUID()));
        verify(drivingSchoolRegistryRepository, never()).save(any());
    }

    /**
     * Vérifie qu'une IllegalStateException est levée si l'on tente de rejeter
     * un registre déjà approuvé — une auto-école active ne peut pas être rejetée.
     */
    @Test
    void rejectRegistry_AlreadyApproved_ThrowsIllegalStateException() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        assertThrows(IllegalStateException.class,
                () -> service.rejectRegistry(registry.getId()));
        verify(drivingSchoolRegistryRepository, never()).save(any());
    }

    /**
     * Vérifie qu'une IllegalStateException est levée si l'on tente de rejeter
     * un registre ayant le statut ACTIVE.
     */
    @Test
    void rejectRegistry_AlreadyActive_ThrowsIllegalStateException() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        assertThrows(IllegalStateException.class,
                () -> service.rejectRegistry(registry.getId()));
        verify(drivingSchoolRegistryRepository, never()).save(any());
    }

    // ─── suspendRegistry ──────────────────────────────────────────────────────

    /**
     * Vérifie qu'un registre en statut ACTIVE peut être suspendu et que le statut
     * est correctement mis à jour à SUSPENDED puis persisté.
     */
    @Test
    void suspendRegistry_WhenActive_Success() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        service.suspendRegistry(registry.getId());

        assertEquals(DrivingSchoolStatus.SUSPENDED, registry.getDrivingSchoolStatus());
        verify(drivingSchoolRegistryRepository).save(registry);
    }

    /**
     * Vérifie qu'un registre en statut APPROVED peut également être suspendu.
     */
    @Test
    void suspendRegistry_WhenApproved_Success() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        service.suspendRegistry(registry.getId());

        assertEquals(DrivingSchoolStatus.SUSPENDED, registry.getDrivingSchoolStatus());
        verify(drivingSchoolRegistryRepository).save(registry);
    }

    /**
     * Vérifie qu'une IllegalStateException est levée si l'on tente de suspendre
     * un registre qui n'est ni ACTIVE ni APPROVED (ex. PENDING ou REJECTED).
     */
    @Test
    void suspendRegistry_WhenNotActiveOrApproved_ThrowsIllegalStateException() {
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        assertThrows(IllegalStateException.class,
                () -> service.suspendRegistry(registry.getId()));
        verify(drivingSchoolRegistryRepository, never()).save(any());
    }

    /**
     * Vérifie qu'une IllegalArgumentException est levée si l'UUID du registre
     * à suspendre est introuvable.
     */
    @Test
    void suspendRegistry_NotFound_ThrowsIllegalArgumentException() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.suspendRegistry(UUID.randomUUID()));
    }

    // ─── deleteRegistry ───────────────────────────────────────────────────────

    /**
     * Vérifie qu'un registre existant est bien supprimé définitivement via le repository,
     * sans contrainte sur son statut actuel.
     */
    @Test
    void deleteRegistry_Success() {
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        service.deleteRegistry(registry.getId());

        verify(drivingSchoolRegistryRepository).delete(registry);
    }

    /**
     * Vérifie qu'une IllegalArgumentException est levée si l'UUID du registre
     * à supprimer est introuvable en base.
     */
    @Test
    void deleteRegistry_NotFound_ThrowsIllegalArgumentException() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteRegistry(UUID.randomUUID()));
        verify(drivingSchoolRegistryRepository, never()).delete(any());
    }

    // ─── getAllRegistries ──────────────────────────────────────────────────────

    /**
     * Vérifie que la pagination sans filtre de statut retourne toutes les entrées
     * via la méthode findAllWithAdmin du repository.
     */
    @Test
    void getAllRegistries_WithoutStatusFilter_ReturnsAllPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DrivingSchoolRegistry> page = new PageImpl<>(List.of(registry));
        when(drivingSchoolRegistryRepository.findAllWithAdmin(pageable)).thenReturn(page);

        Page<DrivingSchoolRegistryPageDTO> result = service.getAllRegistries(pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(drivingSchoolRegistryRepository).findAllWithAdmin(pageable);
        verify(drivingSchoolRegistryRepository, never())
                .findByDrivingSchoolStatus(any(), any());
    }

    /**
     * Vérifie que la pagination avec un filtre de statut retourne uniquement les entrées
     * correspondant au statut demandé via findByDrivingSchoolStatus.
     */
    @Test
    void getAllRegistries_WithStatusFilter_ReturnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DrivingSchoolRegistry> page = new PageImpl<>(List.of(registry));
        when(drivingSchoolRegistryRepository.findByDrivingSchoolStatus(DrivingSchoolStatus.PENDING, pageable))
                .thenReturn(page);

        Page<DrivingSchoolRegistryPageDTO> result = service.getAllRegistries(pageable, DrivingSchoolStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(drivingSchoolRegistryRepository)
                .findByDrivingSchoolStatus(DrivingSchoolStatus.PENDING, pageable);
    }

    // ─── getRegistryById ──────────────────────────────────────────────────────

    /**
     * Vérifie que les détails complets d'un registre sont correctement retournés
     * lorsque l'UUID fourni correspond à une entrée existante.
     */
    @Test
    void getRegistryById_Success() {
        when(drivingSchoolRegistryRepository.findById(registry.getId()))
                .thenReturn(Optional.of(registry));

        DrivingSchoolRegistryPageDTO result = service.getRegistryById(registry.getId());

        assertNotNull(result);
        assertEquals(registry.getId(), result.id());
        assertEquals("MV Auto École", result.schoolName());
        assertEquals("Jean Dupont", result.monitorName());
    }

    /**
     * Vérifie qu'une IllegalArgumentException est levée si l'UUID fourni ne correspond
     * à aucun registre existant.
     */
    @Test
    void getRegistryById_NotFound_ThrowsIllegalArgumentException() {
        when(drivingSchoolRegistryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getRegistryById(UUID.randomUUID()));
    }

    // ─── getRegistryStats ─────────────────────────────────────────────────────

    /**
     * Vérifie que les statistiques du tableau de bord sont correctement agrégées :
     * total, actives (ACTIVE + APPROVED), en attente, rejetées, suspendues, moniteurs,
     * et nouvelles inscriptions du mois. La valeur "actives" est la somme de ACTIVE et APPROVED.
     */
    @Test
    void getRegistryStats_ReturnsAggregatedStats() {
        when(drivingSchoolRegistryRepository.count()).thenReturn(10L);
        when(drivingSchoolRegistryRepository.countByDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE)).thenReturn(3L);
        when(drivingSchoolRegistryRepository.countByDrivingSchoolStatus(DrivingSchoolStatus.APPROVED)).thenReturn(2L);
        when(drivingSchoolRegistryRepository.countByDrivingSchoolStatus(DrivingSchoolStatus.PENDING)).thenReturn(2L);
        when(drivingSchoolRegistryRepository.countByDrivingSchoolStatus(DrivingSchoolStatus.REJECTED)).thenReturn(2L);
        when(drivingSchoolRegistryRepository.countByDrivingSchoolStatus(DrivingSchoolStatus.SUSPENDED)).thenReturn(1L);
        when(drivingSchoolRegistryRepository.countByCreatedAtAfter(any(LocalDate.class))).thenReturn(4L);
        when(drivingSchoolRegistryRepository.countByAdminRole(Role.MONITOR)).thenReturn(7L);

        SchoolRegistryStatsResponse stats = service.getRegistryStats();

        assertNotNull(stats);
        assertEquals(10L, stats.totalRegistries());
        assertEquals(5L, stats.activeRegistries());   // ACTIVE(3) + APPROVED(2)
        assertEquals(2L, stats.pendingRegistries());
        assertEquals(2L, stats.rejectedRegistries());
        assertEquals(1L, stats.suspendedRegistries());
        assertEquals(7L, stats.totalMonitors());
        assertEquals(4L, stats.newThisMonth());
    }
}